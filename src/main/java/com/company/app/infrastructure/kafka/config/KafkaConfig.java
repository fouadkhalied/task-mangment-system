package com.company.app.infrastructure.kafka.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;

/**
 * Kafka Configuration
 * Configures Kafka producers and consumers for task management events
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:task-management-group}")
    private String groupId;

    // ===== PRODUCER CONFIGURATION =====

    /**
     * Producer Factory for creating Kafka producers
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Producer reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas to acknowledge
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry failed sends
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Prevent duplicate messages
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // Ensure ordering

        // Performance settings
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Batch size in bytes
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5); // Wait up to 5ms to batch messages
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Compress messages

        // Timeout settings
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 2 minutes

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka Template for sending messages
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ===== CONSUMER CONFIGURATION =====

    /**
     * Consumer Factory for creating Kafka consumers
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Consumer reliability settings
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Start from beginning if no offset
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit for reliability
        configProps.put("spring.json.trusted.packages", "*"); // Trust all packages for deserialization

        // Performance settings
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024); // Minimum bytes to fetch
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // Maximum wait time
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Maximum records per poll

        // Session and heartbeat timeouts
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000); // 30 seconds
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000); // 10 seconds

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka Listener Container Factory for processing messages
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Container settings
        factory.setConcurrency(3); // Number of concurrent consumers
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE); // Manual commit
        factory.getContainerProperties().setPollTimeout(3000); // Poll timeout in milliseconds

        // Error handling
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());

        return factory;
    }

    // ===== TOPIC CONFIGURATION =====

    /**
     * Task Events Topic Configuration
     */
    @Bean
    public String taskEventsTopic() {
        return "task-events";
    }

    /**
     * Task Notifications Topic Configuration
     */
    @Bean
    public String taskNotificationsTopic() {
        return "task-notifications";
    }

    /**
     * Task Analytics Topic Configuration
     */
    @Bean
    public String taskAnalyticsTopic() {
        return "task-analytics";
    }

    /**
     * Dead Letter Queue Topic Configuration
     */
    @Bean
    public String deadLetterQueueTopic() {
        return "task-events-dlq";
    }
}

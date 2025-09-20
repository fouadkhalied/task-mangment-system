Kafka Implementation
Overview

An advanced messaging system has been implemented using Apache Kafka to handle events and inter-service communication in the task management system.

Main Components
1. Kafka Configuration (KafkaConfig.java)

Location: src/main/java/com/company/app/infrastructure/kafka/config/KafkaConfig.java

Purpose: Configure Kafka producers and consumers with advanced reliability settings

Features:

Producer config with acks=all and idempotence

Consumer config with manual commit

Performance and compression settings

Error handling and Dead Letter Queue

2. Kafka Service (KafkaService.java)

Location: src/main/java/com/company/app/infrastructure/kafka/service/KafkaService.java

Purpose: Publish events and messages via Kafka

Features:

Publish task events

Publish notifications

Publish analytics

Dead Letter Queue handling

Batch operations

3. Task Events

Location: src/main/java/com/company/app/modules/taskCore/domain/event/

Purpose: Define different event types

Event Types:

TaskCreatedEvent – when a new task is created

TaskUpdatedEvent – when a task is updated

TaskStatusChangedEvent – when a task status changes

TaskDeletedEvent – when a task is deleted

TaskAssignedEvent – when a task is assigned

TaskOverdueEvent – when a task becomes overdue

4. Task Event Listener (TaskEventListener.java)

Location: src/main/java/com/company/app/infrastructure/kafka/consumer/TaskEventListener.java

Purpose: Receive and process incoming events

Features:

Handle task events

Handle notifications

Handle analytics

Handle Dead Letter Queue

5. Task Event Processor (TaskEventProcessor.java)

Location: src/main/java/com/company/app/infrastructure/kafka/consumer/TaskEventProcessor.java

Purpose: Process events and execute appropriate actions

Actions:

Update indexes

Send notifications

Update statistics

Trigger automated workflows

Topics Used
1. task-events

Purpose: Core task events

Content: All task-related events (create, update, delete, etc.)

Consumers: All services interested in task events

2. task-notifications

Purpose: User notifications

Content: Notification messages (email, push, SMS)

Consumers: Notification services

3. task-analytics

Purpose: Analytics and statistics

Content: Analytical and measurement data

Consumers: Analytics and BI systems

4. task-events-dlq

Purpose: Dead Letter Queue for failed messages

Content: Messages that failed processing

Consumers: Monitoring and admin systems

Management APIs
1. Kafka Management

Base URL: /api/kafka

Endpoints:

GET /health – Kafka health status

GET /metrics – Kafka statistics

GET /metrics/publish-times – Average publish times

GET /metrics/consumption-times – Average consumption times

POST /metrics/reset – Reset statistics

GET /info – Service info

Performance Monitoring
Publish Metrics:

Number of published events

Number of successfully published events

Number of failed events

Success rate

Average publish times

Consumption Metrics:

Number of consumed events

Number of successfully consumed events

Number of failed events

Success rate

Average consumption times

Kafka Settings
In application.properties:
# Kafka Configuration
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
spring.kafka.consumer.group-id=task-management-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.acks=all
spring.kafka.producer.retries=3
spring.kafka.producer.enable-idempotence=true

In docker-compose.yml:
services:
  kafka:
    image: confluentinc/cp-kafka:7.4.0
    ports:
      - "9092:9092"
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9092
      - KAFKA_AUTO_CREATE_TOPICS_ENABLE=true

  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    ports:
      - "2181:2181"
    environment:
      - ZOOKEEPER_CLIENT_PORT=2181

System Usage
1. Publishing Events:
// Publish a task creation event
TaskCreatedEvent event = TaskCreatedEvent.taskCreatedEventBuilder()
    .taskId("task-123")
    .userId("user-456")
    .boardId("board-789")
    .title("New Task")
    .build();

kafkaService.publishTaskEvent(event);

2. Processing Events:
@KafkaListener(topics = "task-events", groupId = "task-management-group")
public void handleTaskEvent(@Payload TaskEvent event, Acknowledgment acknowledgment) {
    // Process the event
    processTaskEvent(event);
    // Acknowledge processing
    acknowledgment.acknowledge();
}

3. Monitoring Performance:
# Get Kafka metrics
GET /api/kafka/metrics

# Get Kafka health
GET /api/kafka/health

# Get service info
GET /api/kafka/info

Best Practices
1. Reliability:

Use acks=all for reliable publishing

Manual commit for reliable consumption

Dead Letter Queue for failed messages

Idempotence to prevent duplicates

2. Performance:

Message compression (Snappy)

Batch processing

Connection pooling

Partitioning strategies

3. Monitoring:

Track all events

Monitor success rates

Error alerts

Detailed statistics

4. Security:

Message encryption

Authentication & authorization

Network security

Audit logging

Troubleshooting
Common Issues:

Kafka Connection Failed

# Check Kafka status
docker logs kafka-container

# Test connection
kafka-topics --bootstrap-server localhost:9092 --list


Message Processing Failed

# Check Dead Letter Queue
GET /api/kafka/metrics

# Check logs
docker logs app-container


Performance Issues

# Check performance statistics
GET /api/kafka/metrics/publish-times
GET /api/kafka/metrics/consumption-times

Testing
Running Tests:
# Unit tests (without Kafka)
mvn test -Dtest=KafkaServiceUnitTest

# Integration tests (with Embedded Kafka)
mvn test -Dtest=KafkaIntegrationTest

# All Kafka tests
mvn test -Dtest="*Kafka*Test"

Test Requirements:

Embedded Kafka for local testing

Mock objects for unit tests

Test containers for integration tests

Future Development
Planned Features:

Schema Registry – for managing message schemas

Kafka Streams – for real-time data processing

Kafka Connect – for integration with external systems

Monitoring Tools – advanced monitoring

Security Enhancements – stronger security features

Performance Improvements:

Partitioning Strategy – optimized partitioning

Compression Algorithms – different compression methods

Batch Processing – grouped processing

Caching Integration – integration with Redis
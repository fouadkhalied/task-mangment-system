FROM confluentinc/cp-kafka:7.5.0

# Set environment variables with defaults that can be overridden
ENV KAFKA_BROKER_ID=1
ENV KAFKA_ZOOKEEPER_CONNECT=task-mangment-zookeeper:2181
ENV KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://task-mangment-kafka:9092
ENV KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092
ENV KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1
ENV KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1
ENV KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1
ENV KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0
ENV KAFKA_AUTO_CREATE_TOPICS_ENABLE=true
ENV KAFKA_DELETE_TOPIC_ENABLE=true

# Create data directory
RUN mkdir -p /var/lib/kafka/data
RUN chown -R appuser:appuser /var/lib/kafka

# Expose port
EXPOSE 9092

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD kafka-topics --bootstrap-server localhost:9092 --list > /dev/null 2>&1 || exit 1

# Start Kafka
CMD ["/etc/confluent/docker/run"]
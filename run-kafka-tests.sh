#!/bin/bash

echo "Starting Kafka Tests..."

echo ""
echo "1. Starting Redis and Kafka for testing..."
docker run -d --name redis-test -p 6379:6379 redis:7-alpine
docker run -d --name zookeeper-test -p 2181:2181 confluentinc/cp-zookeeper:7.4.0
sleep 5
docker run -d --name kafka-test -p 9092:9092 --link zookeeper-test:zookeeper confluentinc/cp-kafka:7.4.0
sleep 10

echo ""
echo "2. Running Kafka unit tests (no Kafka required)..."
mvn test -Dtest=KafkaServiceUnitTest

echo ""
echo "3. Running Kafka integration tests (requires Kafka)..."
mvn test -Dtest=KafkaIntegrationTest

echo ""
echo "4. Cleaning up..."
docker stop redis-test kafka-test zookeeper-test
docker rm redis-test kafka-test zookeeper-test

echo ""
echo "Kafka tests completed!"

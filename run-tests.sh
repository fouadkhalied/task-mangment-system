#!/bin/bash

echo "Starting Cache Tests..."

echo ""
echo "1. Starting Redis for testing..."
docker run -d --name redis-test -p 6379:6379 redis:7-alpine
sleep 3

echo ""
echo "2. Running unit tests (no Redis required)..."
mvn test -Dtest=CacheServiceUnitTest

echo ""
echo "3. Running cache integration tests (requires Redis)..."
mvn test -Dtest=CacheIntegrationTest

echo ""
echo "4. Running Kafka unit tests (no Kafka required)..."
mvn test -Dtest=KafkaServiceUnitTest

echo ""
echo "5. Running Kafka integration tests (requires Kafka)..."
mvn test -Dtest=KafkaIntegrationTest

echo ""
echo "6. Cleaning up..."
docker stop redis-test
docker rm redis-test

echo ""
echo "Tests completed!"

# System Architecture Overview

## Project: Task Management System
**Version**: 1.0  
**Date**: September 2025  
**Status**: Active Development

---

## Executive Summary

The Task Management System implements an event-driven microservices architecture designed for scalability, maintainability, and cost-effectiveness. The system uses Azure Container Apps as the hosting platform, providing serverless scaling and simplified container orchestration.

## Architecture Principles

### 1. Single Entry Point
- All external client requests flow through the App Service
- No direct API access to worker services
- Simplified security and routing management

### 2. Event-Driven Communication
- Services communicate through Kafka event streams
- Loose coupling between services
- Asynchronous processing for non-critical operations

### 3. Database per Service Pattern
- Each service owns its data store
- Optimized databases for specific workloads
- Data consistency through eventual consistency patterns

## System Components

### Core Services

#### App Service (Spring Boot)
- **Responsibility**: Main application logic and user-facing APIs
- **Technology**: Spring Boot with PostgreSQL
- **Deployment**: Azure Container Apps
- **APIs**: REST endpoints for task management, user operations
- **Events**: Publishes domain events to Kafka topics

#### Notification Worker (Spring Boot)  
- **Responsibility**: Process notification events and send alerts
- **Technology**: Spring Boot with PostgreSQL (notifications schema)
- **Deployment**: Azure Container Apps
- **Communication**: Kafka consumer only (no external APIs)
- **External Integration**: SendGrid for email, push notification services

#### Analytics Worker (Python/FastAPI)
- **Responsibility**: Process events for metrics and historical analysis
- **Technology**: Python with FastAPI and ClickHouse
- **Deployment**: Azure Container Apps
- **Communication**: Kafka consumer only (no external APIs)
- **Data**: Stores event history and aggregated metrics

### Data Stores

#### PostgreSQL (Core Data)
- Primary business data (users, tasks, projects)
- ACID transactions for data consistency
- Optimized for transactional workloads

#### PostgreSQL (Notifications)
- High read/write volume notification data
- Separate schema or instance for isolation
- Optimized indexes for notification queries

#### ClickHouse (Analytics)
- Column-oriented storage for analytics
- Event history and time-series data
- Optimized for analytical queries and aggregations

#### Redis (Cache & Sessions)
- Application caching layer
- WebSocket session management
- Temporary data storage

### Message Broker

#### Kafka (Confluent Cloud)
- Event streaming between services
- Topics: task-events, user-events, notification-events
- Guarantees message ordering and delivery
- Free tier suitable for development and small production loads

## Data Flow

### Primary User Operations
1. User interacts with Frontend (React/Next.js)
2. Frontend calls App Service REST APIs
3. App Service processes business logic
4. App Service updates PostgreSQL database
5. App Service publishes domain events to Kafka
6. Worker services consume events asynchronously

### Event Processing
1. Notification Worker consumes events → sends emails/alerts → stores in notifications database
2. Analytics Worker consumes events → processes metrics → stores in ClickHouse

## Deployment Architecture

### Azure Container Apps Environment
- All services deployed as containers
- Automatic scaling including scale-to-zero
- Built-in load balancing and service discovery
- Internal networking for service communication

### External Dependencies
- **SendGrid**: Email delivery service
- **Azure Blob Storage**: File attachments and media
- **Azure AD B2C**: Authentication and authorization
- **Confluent Cloud**: Managed Kafka service

## Scalability Considerations

### Horizontal Scaling
- Container Apps automatically scale based on load
- Kafka partitioning enables parallel processing
- Database read replicas for read-heavy workloads

### Performance Optimization
- Redis caching reduces database load
- ClickHouse optimized for analytical queries
- Event-driven architecture prevents blocking operations

## Security Model

### Authentication & Authorization
- OAuth 2.0 with Azure AD B2C
- JWT tokens for API authentication
- Role-based access control (RBAC)

### Network Security
- Internal service communication within Container Apps environment
- HTTPS termination at application gateway
- Database access restricted to application services

## Monitoring & Observability

### Application Monitoring
- Azure Monitor for container metrics
- Custom metrics for business KPIs
- Distributed tracing across services

### Data Monitoring
- Database performance monitoring
- Kafka topic lag monitoring
- Cache hit/miss rates

---

## Next Steps

1. Review architecture decisions document (`02-architecture-decisions.md`)
2. Examine detailed service specifications
3. Set up development environment following deployment guides
# Kafka Flow

Order Service

↓

Publishes OrderCreatedEvent

↓

Kafka Topic: order-events

↓

Notification Service

↓

Creates Notification

↓

Caches Notification in Redis
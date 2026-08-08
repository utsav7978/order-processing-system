# Event-Driven Order Processing System

> Skeleton in progress — this README will be fully written in Phase 12.
> See `docs/` (added from Phase 7 onward) for architecture, Kafka flow, and Redis usage notes as they're built.

## Services
- `gateway` — Spring Cloud Gateway, JWT validation, routing
- `user-service` — registration, login, JWT issuing, profile
- `product-service` — product CRUD, Redis-cached
- `order-service` — order creation, publishes `OrderCreatedEvent` to Kafka
- `notification-service` — consumes `OrderCreatedEvent`, logs a simulated email send, stores notifications

## Status
Phase 2 complete: project skeleton (empty-but-compiling Spring Boot apps for all 5 services).

┌─────────────────────┐
                                   │      Clients         │
                                   │ (Postman / Frontend) │
                                   └──────────┬───────────┘
                                              │  HTTP
                                   ┌──────────▼───────────┐
                                   │     API Gateway       │
                                   │ Spring Cloud Gateway  │
                                   │  + JWT Validation     │
                                   └──────────┬───────────┘
                    ┌──────────────┬──────────┼───────────────┬───────────────┐
                    │              │          │               │               │
             ┌──────▼─────┐ ┌──────▼─────┐ ┌──▼──────────┐ ┌──▼─────────────┐
             │ User Service│ │Product Svc │ │Order Service │ │Notification Svc│
             │  (MySQL)    │ │(MySQL+Redis)│ │(MySQL+Kafka) │ │(Redis+Kafka)   │
             └─────────────┘ └────────────┘ └──────┬───────┘ └───────▲────────┘
                                                     │  Kafka Topic     │
                                                     │  order-events    │
                                                     └──────────────────┘
This project is a high-performance, Service-Oriented Architecture (SOA) based Airline Management System. It features a Spring Boot backend protected and managed by a Spring Cloud Gateway.

Note: This project strictly follows SOA principles. The core business logic is located in this repository, while security and rate-limiting are handled by a separate API Gateway repository.

Deployed Swagger UI: https://airline-api-project-e0h2dwbpgyh3h3d8.germanywestcentral-01.azurewebsites.net/swagger-ui/index.html
API Gateway Source Code: [https://github.com/barishansu45/api-gateway]
* **Presentation Video:** [Link will be updated shortly after recording]

* Design & Architecture
* SOA Architecture: The system is decoupled into a Gateway and a Backend service to ensure scalability and security.
* DTO Usage: Data Transfer Objects (DTOs) are used for all API communications to avoid direct database entity exposure.
* Versioned API: All REST services are versioned under /api/v1/.

* Assumptions
* Seat Assignment: A simple unique numbering system (e.g., Seat-1) is assigned to passengers during check-in.
* Capacity Logic: Flight capacity decreases by 1 for each ticket sold. If capacity reaches 0, the system returns a "Sold Out" status.
* Pagination: Query results are paginated with a default size of 10.

Issues Encountered
* CSV Bulk Upload: Encountered formatting issues with date-time fields during file processing. This was resolved by enforcing the ISO 8601 standard.
* Rate Limiting: Implementing the "3 calls per day" limit required a custom filter in the Gateway to track user requests accurately.


* The following diagram represents the database schema and entity relationships:
erDiagram
    FLIGHT {
        Long id PK
        String flightNumber
        DateTime dateFrom
        DateTime dateTo
        String airportFrom
        String airportTo
        Integer duration
        Integer capacity
    }
    TICKET {
        Long id PK
        String ticketNumber
        String passengerName
        String seatNumber
        Boolean checkedIn
        Long flight_id FK
    }
    FLIGHT ||--o{ TICKET : "has"

  Visual Representation:

  
<img width="245" height="561" alt="ERDiagram" src="https://github.com/user-attachments/assets/1259bb99-d7b9-423b-ab7e-798994f0d62d" />

*Testing Results (k6)

[k6testing.csv](https://github.com/user-attachments/files/26348014/k6testing.csv)Scenario,Virtual Users (VU),Duration
Normal Load,20,30s
Peak Load,50,30s
Stress Load,100,30s

*Performance Metrics

Average Response Time: 64.17 ms

95th Percentile (p95): 86.36 ms

Requests Per Second: ~21.5 req/s

Error Rate: 0%

<img width="916" height="703" alt="K6Test" src="https://github.com/user-attachments/assets/c74f9a71-3920-4347-837a-a0175a216058" />

Analysis
The API maintained stable performance even under the 100 VU Stress Load, with response times consistently under 100ms. 
The API Gateway successfully routed traffic without causing any bottlenecks. To further enhance scalability,
a distributed caching layer (like Redis) could be implemented for frequent flight queries.

*Security & Rate Limiting Proofs

<img width="729" height="126" alt="401Unauthorized" src="https://github.com/user-attachments/assets/9ae29d39-82ba-477b-a292-c28d1e45d294" />

<img width="1470" height="586" alt="429TooMany Requests" src="https://github.com/user-attachments/assets/316d13e6-2aad-47e3-bb7f-99b3fe34b1b2" />




  

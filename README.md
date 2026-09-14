# ForgePath

**ForgePath** is a self-service Internal Developer Platform (IDP) for creating, configuring, deploying, and operating backend services through standardized engineering workflows.

The long-term goal is to let a developer define a service once and have ForgePath automate the repetitive platform work around it: repository creation, service scaffolding, CI/CD, containerization, infrastructure provisioning, Kubernetes deployment, observability, ownership, and deployment lifecycle management.

> **Project status:** Active development — currently building the control-plane foundation and service catalog.

---

## Why ForgePath?

Creating a new backend service often requires much more than writing application code.

A developer may need to:

* create a repository
* configure the project structure
* create a Dockerfile
* configure CI/CD
* provision a database
* write Kubernetes manifests
* configure health checks
* set up observability
* define ownership and environments
* maintain deployment metadata

When every team solves these problems independently, services gradually become inconsistent and expensive to operate.

ForgePath aims to provide an opinionated **golden path** for service creation so developers can focus on application functionality while the platform handles common engineering standards.

Conceptually:

```text
Developer
    ↓
ForgePath Dashboard
    ↓
ForgePath Platform API
    ↓
Service Template / Golden Path
    ↓
GitHub Repository
    ↓
CI/CD
    ↓
Container Registry
    ↓
Kubernetes
    ↓
Observability
```

---

# Current Architecture

ForgePath currently uses a **modular monolith** for the control plane.

```text
                    ┌──────────────────────┐
                    │    ForgePath Web     │
                    │ React + TypeScript   │
                    │      (planned)       │
                    └──────────┬───────────┘
                               │
                               │ REST
                               ▼
                    ┌──────────────────────┐
                    │  ForgePath Platform  │
                    │        API           │
                    │                      │
                    │ Java 21              │
                    │ Spring Boot          │
                    └──────────┬───────────┘
                               │
                               │ JPA / JDBC
                               ▼
                    ┌──────────────────────┐
                    │     PostgreSQL       │
                    │                      │
                    │ Platform State       │
                    └──────────────────────┘
```

The platform backend is intentionally starting as a modular monolith rather than being split prematurely into microservices.

Domain boundaries remain explicit so components such as provisioning workers can later be extracted when independent scaling, asynchronous execution, or fault isolation provides a real architectural benefit.

---

# Current Features

The current implementation includes:

### Platform foundation

* Java 21 target runtime
* Spring Boot 4
* Maven Wrapper
* Spring MVC REST API
* Spring Boot Actuator
* structured application configuration
* PostgreSQL 18
* Docker Compose local infrastructure
* persistent PostgreSQL Docker volume

### Database management

* Flyway schema migrations
* PostgreSQL-backed persistence
* Hibernate/JPA schema validation
* explicit migration history
* Testcontainers-based PostgreSQL integration testing

### Team Catalog

ForgePath currently supports team ownership as the first service-catalog domain.

Implemented endpoints:

```http
POST /api/v1/teams
GET  /api/v1/teams
GET  /api/v1/teams/{teamId}
```

Capabilities include:

* UUID-based team identifiers
* team name validation
* duplicate-team protection
* persisted creation timestamps
* deterministic team ordering
* structured `400 Bad Request` responses
* structured `404 Not Found` responses
* structured `409 Conflict` responses
* global REST exception handling

Example team:

```json
{
  "id": "7ea550be-4a6a-4055-81c2-eb804adb414c",
  "name": "Messaging Team",
  "created_at": "2026-09-13T19:59:43.392141900Z"
}
```

---

# Technology Stack

| Area                 | Technology                                     |
| -------------------- | ---------------------------------------------- |
| Language             | Java 21                                        |
| Backend              | Spring Boot 4                                  |
| API                  | Spring MVC / REST                              |
| Persistence          | Spring Data JPA / Hibernate                    |
| Database             | PostgreSQL 18                                  |
| Migrations           | Flyway                                         |
| Build                | Maven                                          |
| Local Infrastructure | Docker / Docker Compose                        |
| Testing              | JUnit 5, Mockito, Spring Boot Test             |
| Integration Testing  | Testcontainers                                 |
| Frontend             | React + TypeScript *(planned)*                 |
| CI/CD                | GitHub Actions *(planned)*                     |
| Containers           | Docker                                         |
| Orchestration        | Kubernetes / kind *(planned)*                  |
| Infrastructure       | Terraform *(planned)*                          |
| Observability        | OpenTelemetry, Prometheus, Grafana *(planned)* |

---

# Repository Structure

```text
forgepath/
│
├── platform-api/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── io/github/khansabih/forgepath/
│   │   │   │       ├── catalog/
│   │   │   │       │   └── team/
│   │   │   │       │       ├── api/
│   │   │   │       │       ├── application/
│   │   │   │       │       ├── domain/
│   │   │   │       │       ├── exception/
│   │   │   │       │       └── persistence/
│   │   │   │       │
│   │   │   │       └── common/
│   │   │   │           └── error/
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/
│   │   │           └── migration/
│   │   │
│   │   └── test/
│   │
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── compose.yaml
├── .gitignore
└── README.md
```

Future top-level modules will include:

```text
platform-web/
templates/
infrastructure/
docs/
```

They will be introduced as the corresponding platform capabilities are implemented.

---

# Getting Started

## Prerequisites

You will need:

* Java 21+
* Docker Desktop
* Git
* Maven is optional because the repository includes the Maven Wrapper

Check Java:

```bash
java -version
```

Check Docker:

```bash
docker --version
docker compose version
```

---

## Clone the repository

```bash
git clone https://github.com/khansabih/forgepath.git
cd forgepath
```

---

# Start PostgreSQL

ForgePath uses PostgreSQL locally through Docker Compose.

```bash
docker compose up -d postgres
```

Check the container:

```bash
docker compose ps
```

You should see the PostgreSQL container running and exposing:

```text
localhost:5432
```

Local development configuration currently uses:

```text
Database: forgepath
Username: forgepath
Password: forgepath_local
```

These credentials are intentionally limited to local development.

Production secrets will not be committed to the repository.

---

# Run the Platform API

On Windows:

```powershell
cd platform-api
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
cd platform-api
./mvnw spring-boot:run
```

ForgePath should start its embedded Tomcat server.

The health endpoint is available at:

```text
http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

---

# Database Migrations

ForgePath uses **Flyway** rather than allowing Hibernate to automatically modify the database schema.

Migration files live under:

```text
platform-api/src/main/resources/db/migration/
```

Current migration history includes:

```text
V1__create_service_catalog.sql
V2__rename_team_create_at_column.sql
```

Hibernate is configured with:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

This means:

```text
Flyway
    ↓
owns schema changes

Hibernate
    ↓
validates entity mappings against the schema
```

This prevents silent database modifications and keeps schema evolution explicit, versioned, and reviewable.

---

# API Examples

## Create a team

```http
POST /api/v1/teams
Content-Type: application/json
```

Request:

```json
{
  "name": "Messaging Team"
}
```

Example response:

```http
201 Created
```

```json
{
  "id": "7ea550be-4a6a-4055-81c2-eb804adb414c",
  "name": "Messaging Team",
  "created_at": "2026-09-13T19:59:43.392141900Z"
}
```

---

## List teams

```http
GET /api/v1/teams
```

Example response:

```json
[
  {
    "id": "7ea550be-4a6a-4055-81c2-eb804adb414c",
    "name": "Messaging Team",
    "created_at": "2026-09-13T19:59:43.392141900Z"
  }
]
```

---

## Get team by ID

```http
GET /api/v1/teams/{teamId}
```

Example:

```text
GET /api/v1/teams/7ea550be-4a6a-4055-81c2-eb804adb414c
```

---

# Error Handling

ForgePath exposes a consistent API error format.

Example duplicate-team request:

```http
409 Conflict
```

```json
{
  "timestamp": "2026-09-13T20:05:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Team already exists with name: Messaging Team",
  "path": "/api/v1/teams",
  "validationErrors": {}
}
```

Validation errors return:

```http
400 Bad Request
```

Missing resources return:

```http
404 Not Found
```

---

# Testing

Run the full test suite:

### Windows

```powershell
.\mvnw.cmd clean test
```

### macOS/Linux

```bash
./mvnw clean test
```

ForgePath uses Testcontainers to start a temporary PostgreSQL instance for integration testing.

This means automated tests do **not** depend on the persistent local development database.

Conceptually:

```text
JUnit
  ↓
Testcontainers
  ↓
Temporary PostgreSQL
  ↓
Flyway migrations
  ↓
Hibernate validation
  ↓
Spring Boot
  ↓
Tests
  ↓
Temporary container removed
```

This keeps the test environment isolated and reproducible.

Additional focused unit and web-layer tests are being introduced as catalog capabilities are expanded.

---

# Platform Engineering Principles

ForgePath is being designed around several principles rather than simply collecting DevOps technologies.

### Golden Paths

Developers should receive a supported, production-ready path for creating common service types.

The first golden path will target:

```text
Java 21
Spring Boot
Maven
Actuator
structured logging
Docker
health checks
testing
CI/CD
Kubernetes
OpenTelemetry
```

---

### Self-Service with Guardrails

ForgePath should remove repetitive infrastructure work without removing platform governance.

Future workflows will combine:

```text
Developer Intent
      ↓
Platform Validation
      ↓
Authorization
      ↓
Provisioning
      ↓
Audit Trail
```

---

### Infrastructure Should Be Automated, Not Hidden

Developers should not need to manually configure every Kubernetes or CI/CD primitive for normal services.

They should still have visibility into:

* repository
* build
* image
* deployment
* environment
* health
* metrics
* dependencies
* provisioning state

---

### Explicit Schema Management

Database evolution is managed using Flyway migrations rather than automatic Hibernate schema mutation.

---

### Realistic Failure Handling

Future provisioning workflows will treat operations such as:

```text
Create repository
Generate source
Provision infrastructure
Deploy workload
```

as independent, observable steps rather than one large synchronous HTTP request.

This will allow ForgePath to support:

* retries
* idempotency
* partial-failure recovery
* auditability
* asynchronous provisioning

---

# Roadmap

## Phase 1 — Platform Foundation

* [x] Spring Boot platform API
* [x] PostgreSQL
* [x] Docker Compose
* [x] Flyway migrations
* [x] Spring Data JPA
* [x] Testcontainers
* [x] Team catalog
* [ ] Service catalog
* [ ] React + TypeScript dashboard

## Phase 2 — Service Creation

* [ ] Create Service workflow
* [ ] service ownership
* [ ] environments
* [ ] Spring Boot golden-path definition
* [ ] source-code generation
* [ ] provisioning workflow model

## Phase 3 — GitHub Integration

* [ ] GitHub authentication strategy
* [ ] GitHub App integration
* [ ] repository creation
* [ ] generated source push
* [ ] repository metadata

## Phase 4 — Containerization

* [ ] generated Dockerfiles
* [ ] container builds
* [ ] container registry integration

## Phase 5 — Kubernetes

* [ ] local Kubernetes with kind
* [ ] namespaces
* [ ] Deployments
* [ ] Services
* [ ] ConfigMaps
* [ ] Secrets
* [ ] readiness probes
* [ ] liveness probes

## Phase 6 — CI/CD

* [ ] generated GitHub Actions workflows
* [ ] automated tests
* [ ] image builds
* [ ] image publishing
* [ ] automated deployment

## Phase 7 — Observability

* [ ] OpenTelemetry
* [ ] Prometheus
* [ ] Grafana
* [ ] request metrics
* [ ] p50 / p95 / p99 latency
* [ ] error rate
* [ ] CPU and memory metrics
* [ ] deployment health

## Phase 8 — Platform Features

* [ ] RBAC
* [ ] users and teams
* [ ] environments
* [ ] deployment history
* [ ] environment promotion
* [ ] rollback
* [ ] audit logging

## Phase 9 — Advanced Platform Engineering

* [ ] Terraform infrastructure automation
* [ ] template versioning
* [ ] service scorecards
* [ ] dependency graph
* [ ] policy enforcement
* [ ] DORA metrics
* [ ] GitOps evaluation
* [ ] developer documentation

---

# Future Developer Experience

The target user experience is eventually:

```text
Create Service

Name:
notification-service

Owner:
Messaging Team

Framework:
Spring Boot

Runtime:
Java 21

Database:
PostgreSQL

Environments:
✓ Development
✓ Staging

Features:
✓ CI/CD
✓ Health Checks
✓ OpenTelemetry
✓ PostgreSQL
```

ForgePath would then orchestrate:

```text
Create service record
        ↓
Generate Spring Boot project
        ↓
Create GitHub repository
        ↓
Push generated source
        ↓
Configure CI/CD
        ↓
Build container
        ↓
Provision required infrastructure
        ↓
Deploy to Kubernetes
        ↓
Configure observability
        ↓
Register deployment state
```

---

# Planned Service Catalog

A completed catalog entry will eventually expose information similar to:

```text
Notification Service

Owner
Messaging Team

Framework
Spring Boot

Runtime
Java 21

Repository
github.com/example/notification-service

Environments
Development
Staging
Production

Health
Healthy

Dependencies
PostgreSQL
customer-service

Latest Version
v1.3.2
```

---

# Terraform Strategy

Terraform will be introduced where it provides the strongest architectural value.

The intention is to primarily use Terraform for longer-lived infrastructure such as:

```text
cloud networking
Kubernetes infrastructure
container registries
managed databases
IAM
platform-level resources
```

rather than forcing every application deployment through Terraform simply to demonstrate the technology.

Application delivery will use tooling appropriate to the workload lifecycle, such as Kubernetes manifests, Helm/Kustomize, GitHub Actions, and potentially GitOps.

---

# Optional AI / MCP Direction

AI is deliberately **not** required for ForgePath's core value proposition.

Once the underlying platform is mature, ForgePath may expose controlled AI/MCP workflows such as:

```text
"Create a Spring Boot notification service with PostgreSQL
and deploy it to staging."
```

Any such integration would operate through existing ForgePath APIs and enforce:

* RBAC
* restricted tool access
* auditable actions
* policy validation
* human approval for destructive or production operations

The AI would act as another platform client rather than receiving unrestricted infrastructure access.

---

# Project Goals

ForgePath is being built as a portfolio project for demonstrating practical experience across:

* backend engineering
* Java / Spring Boot
* REST API design
* relational data modelling
* distributed-systems concepts
* Docker
* CI/CD
* Kubernetes
* Terraform
* platform engineering
* cloud-native architecture
* observability
* authorization
* infrastructure automation
* developer experience

The project intentionally prioritizes understanding architectural trade-offs over adding technologies purely for résumé keywords.

---

# Status

🚧 **ForgePath is under active development.**

The current focus is completing the service catalog and automated test suite before moving into golden-path generation and provisioning orchestration.

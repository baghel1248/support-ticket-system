# Implementation Tasks: Support Ticket Management System

## Phase 1: Core Domain & State Machine (TDD)
- [ ] **Task 1.1: State Machine Enums & Exception**
    - Create `TicketStatus` enum (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`).
    - Create `Priority` enum (`LOW`, `MEDIUM`, `HIGH`, `URGENT`).
    - Create `InvalidStateTransitionException` class.
- [ ] **Task 1.2: State Machine Validator Tests (TDD - Red Phase)**
    - Write parameterized JUnit 5 unit tests verifying all ALLOWED state transitions in `spec/state-machine.md`.
    - Write parameterized JUnit 5 unit tests verifying all FORBIDDEN state transitions expect `InvalidStateTransitionException`.
    - *Verify tests fail before writing implementation.*
- [ ] **Task 1.3: State Machine Validator Implementation (TDD - Green Phase)**
    - Implement `TicketStateTransitionValidator` component.
    - Run tests and confirm 100% pass rate.

## Phase 2: Persistence Layer
- [ ] **Task 2.1: Entities & Repositories**
    - Create `Ticket` entity mapped with `@Enumerated(EnumType.STRING)` and `@Version`.
    - Create `Comment` entity mapped with `@ManyToOne` relationship to `Ticket`.
    - Create `TicketRepository` (Spring Data JPA) with keyword search and status filter specs.
    - Create `CommentRepository`.
- [ ] **Task 2.2: Repository Integration Tests**
    - Create `@DataJpaTest` on H2 to verify creation, listing, searching, and filtering.

## Phase 3: Service Layer & Business Logic
- [ ] **Task 3.1: DTOs & Mappers**
    - Create `CreateTicketRequest`, `UpdateTicketRequest`, `UpdateStatusRequest`, `AddCommentRequest`, and `TicketResponse` Java Records with `jakarta.validation` annotations.
- [ ] **Task 3.2: Ticket Service Implementation**
    - Implement `TicketService` enforcing `TicketStateTransitionValidator` on status updates.
- [ ] **Task 3.3: Service Integration Tests**
    - Test business operations and confirm invalid transitions trigger transaction rollback.

## Phase 4: REST Controller & Global Exception Handler
- [ ] **Task 4.1: Global Exception Handler**
    - Create `@RestControllerAdvice` handling `InvalidStateTransitionException` and `MethodArgumentNotValidException` returning RFC-7807/HTTP 400 payloads.
- [ ] **Task 4.2: Ticket Controller**
    - Implement endpoints according to `spec/api-contract.md`.
- [ ] **Task 4.3: Web Integration Tests**
    - Use `@WebMvcTest` / `MockMvc` to test API endpoints, HTTP status codes, and error responses.

## Phase 5: Frontend Interface
- [ ] **Task 5.1: UI Components**
    - Build frontend pages (Ticket List, Ticket Details, Create Ticket Form, Status Transition Buttons).
- [ ] **Task 5.2: Error Display Integration**
    - Connect UI to backend REST API and render backend validation/state errors clearly to the user.

## Phase 6: Code Review & Prompt Audit
- [ ] **Task 6.1: Run AI Review Commands**
    - Run `commands/review-code.md` to check for security secrets, unvalidated DTOs, or missing rules.
- [ ] **Task 6.2: Log AI Corrections**
    - Record at least 2–3 meaningful AI mistakes and manual fixes in `docs/prompt-history.md`.

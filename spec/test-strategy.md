# Test Strategy & Quality Plan

## Unit Testing
- **State Machine Validator**: Isolated tests for `TicketStateTransitionValidator` covering 100% of allowed and forbidden state matrix combinations.

## Integration Testing
- **Repository Layer**: `@DataJpaTest` on H2 in-memory DB to verify custom JPA queries and constraint validations.
- **Controller & Service Layer**: `@SpringBootTest` / `@AutoConfigureMockMvc` testing REST endpoints, verifying HTTP status codes and RFC-7807 error responses for invalid state changes.

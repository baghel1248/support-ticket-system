# Ticket Status State Machine

## Transition matrix

| Current status | Allowed target statuses |
| --- | --- |
| `OPEN` | `IN_PROGRESS`, `CANCELLED` |
| `IN_PROGRESS` | `RESOLVED`, `CANCELLED` |
| `RESOLVED` | `CLOSED` |
| `CLOSED` | None |
| `CANCELLED` | None |

## Rules

- `CLOSED` and `CANCELLED` are terminal states and cannot transition to a different status.
- A same-state transition (`currentStatus == newStatus`) is an allowed no-op.
- Any transition not listed in the matrix throws `InvalidStateTransitionException`.
# Data Model Specification

## Enums
### `TicketStatus`
`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`

### `Priority`
`LOW`, `MEDIUM`, `HIGH`, `URGENT`

## Entities

### `Ticket` Entity
- `id` (Long, Primary Key, Auto-generated)
- `title` (String, Not Null, Max 150 chars)
- `description` (String, Not Null, Max 2000 chars)
- `status` (TicketStatus Enum, String persist, Default: `OPEN`)
- `priority` (Priority Enum, String persist, Default: `LOW`)
- `assignee` (String, Nullable, Max 100 chars)
- `createdAt` (Instant, Auto-set on create)
- `updatedAt` (Instant, Auto-set on update)
- `version` (Long, Optimistic locking via `@Version`)

### `Comment` Entity
- `id` (Long, Primary Key, Auto-generated)
- `ticket` (ManyToOne, Foreign Key `ticket_id`, Not Null)
- `author` (String, Not Null)
- `content` (String, Not Null, Max 1000 chars)
- `createdAt` (Instant, Auto-set on create)
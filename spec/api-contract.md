# REST API Contract

## Endpoints

### 1. Create Ticket
- **Method**: `POST /api/v1/tickets`
- **Request Body**:
```json
{
  "title": "Fix login crash",
  "description": "Users get 500 error on submit",
  "priority": "HIGH",
  "assignee": "alex"
}
Response: 201 Created with created Ticket JSON object.
2. List / Search / Filter Tickets
Method: GET /api/v1/tickets
Query Params: status (optional), keyword (optional)
Response: 200 OK with Array of Ticket objects.
3. Update Status (State Machine Enforcement)
Method: PATCH /api/v1/tickets/{id}/status
Request Body: {"status": "IN_PROGRESS"}
Response: 200 OK on valid transition, 400 Bad Request on invalid transition.
4. Add Comment
Method: POST /api/v1/tickets/{id}/comments
Request Body: {"author": "jane", "content": "Investigating logs."}
Response: 201 Created

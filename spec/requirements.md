# Functional Requirements: Support Ticket Management System

## Scope
The Support Ticket Management System enables users to manage technical support requests through a structured lifecycle.

## Functional Capabilities
1. **Create Ticket**: Users can create a ticket with a mandatory `title` and `description`, optional `priority` (default: `LOW`), and optional `assignee`. Initial status is always `OPEN`.
2. **List Tickets**: Retrieve a list of all tickets sorted by `createdAt` descending.
3. **View Ticket Details**: Fetch detailed information for a single ticket, including linked comments.
4. **Update Ticket**: Update `title`, `description`, `priority`, and `assignee`.
5. **Add Comments**: Add audit comments to a specific ticket with `author` and `content`.
6. **Search**: Search tickets by keyword across `title` and `description`.
7. **Filter**: Filter tickets by exact `status` match.
8. **Input Validation**: Backend must enforce non-blank titles, max lengths, and valid enums.
9. **UI Error Handling**: UI must display API validation errors clearly.
const API_BASE = "/api/v1/tickets";

const STATUS_ACTIONS = {
    OPEN: [
        { status: "IN_PROGRESS", label: "Start Progress" },
        { status: "CANCELLED", label: "Cancel" }
    ],
    IN_PROGRESS: [
        { status: "RESOLVED", label: "Resolve" },
        { status: "CANCELLED", label: "Cancel" }
    ],
    RESOLVED: [
        { status: "CLOSED", label: "Close" }
    ],
    CLOSED: [],
    CANCELLED: []
};

const els = {
    errorBanner: document.getElementById("error-banner"),
    errorText: document.getElementById("error-banner-text"),
    errorDismiss: document.getElementById("error-dismiss"),
    searchForm: document.getElementById("search-form"),
    keyword: document.getElementById("keyword"),
    statusFilter: document.getElementById("status-filter"),
    ticketList: document.getElementById("ticket-list"),
    emptyList: document.getElementById("empty-list"),
    noSelection: document.getElementById("no-selection"),
    ticketDetails: document.getElementById("ticket-details"),
    detailId: document.getElementById("detail-id"),
    detailTitle: document.getElementById("detail-title"),
    detailStatus: document.getElementById("detail-status"),
    detailPriority: document.getElementById("detail-priority"),
    detailAssignee: document.getElementById("detail-assignee"),
    detailCreated: document.getElementById("detail-created"),
    detailUpdated: document.getElementById("detail-updated"),
    detailDescription: document.getElementById("detail-description"),
    statusActions: document.getElementById("status-actions"),
    updateForm: document.getElementById("update-form"),
    updateTitle: document.getElementById("update-title"),
    updateDescription: document.getElementById("update-description"),
    updatePriority: document.getElementById("update-priority"),
    updateAssignee: document.getElementById("update-assignee"),
    commentThread: document.getElementById("comment-thread"),
    commentForm: document.getElementById("comment-form"),
    commentAuthor: document.getElementById("comment-author"),
    commentContent: document.getElementById("comment-content"),
    openCreateModal: document.getElementById("open-create-modal"),
    createModal: document.getElementById("create-modal"),
    createForm: document.getElementById("create-form"),
    cancelCreate: document.getElementById("cancel-create")
};

let selectedTicketId = null;

function clearError() {
    els.errorBanner.hidden = true;
    els.errorText.textContent = "";
}

function showError(message) {
    els.errorText.textContent = message;
    els.errorBanner.hidden = false;
}

function parseRfc7807(body) {
    if (!body || typeof body !== "object") {
        return "Request failed";
    }
    const fieldErrors = Array.isArray(body.errors)
        ? body.errors
            .map((err) => {
                if (typeof err === "string") {
                    return err;
                }
                const field = err.field ? `${err.field}: ` : "";
                return `${field}${err.message || err.detail || ""}`.trim();
            })
            .filter(Boolean)
        : [];
    const primary = body.detail || body.message || body.title;
    if (fieldErrors.length && primary) {
        return `${primary} — ${fieldErrors.join("; ")}`;
    }
    if (fieldErrors.length) {
        return fieldErrors.join("; ");
    }
    return primary || "Request failed";
}

async function apiFetch(path, options = {}) {
    const response = await fetch(path, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });

    if (response.status === 204) {
        return null;
    }

    const raw = await response.text();
    let body = null;
    if (raw) {
        try {
            body = JSON.parse(raw);
        } catch {
            body = raw;
        }
    }

    if (!response.ok) {
        if (response.status === 400 || response.status === 404) {
            showError(typeof body === "string" ? body : parseRfc7807(body));
        } else {
            showError(`Request failed (${response.status})`);
        }
        const error = new Error("API request failed");
        error.status = response.status;
        error.body = body;
        throw error;
    }

    clearError();
    return body;
}

function formatDate(value) {
    if (!value) {
        return "—";
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function badge(el, kind, value) {
    el.className = `badge ${kind}-${value}`;
    el.textContent = value || "—";
}

async function loadTickets() {
    const params = new URLSearchParams();
    const keyword = els.keyword.value.trim();
    const status = els.statusFilter.value;
    if (keyword) {
        params.set("keyword", keyword);
    }
    if (status) {
        params.set("status", status);
    }
    const query = params.toString();
    const tickets = await apiFetch(`${API_BASE}${query ? `?${query}` : ""}`);
    renderTicketList(tickets || []);
}

function renderTicketList(tickets) {
    els.ticketList.replaceChildren();
    els.emptyList.hidden = tickets.length > 0;

    tickets.forEach((ticket) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `ticket-card${ticket.id === selectedTicketId ? " selected" : ""}`;
        button.setAttribute("role", "listitem");
        button.addEventListener("click", () => viewTicketDetails(ticket.id));

        const id = document.createElement("span");
        id.className = "id";
        id.textContent = `#${ticket.id}`;

        const title = document.createElement("h3");
        title.textContent = ticket.title;

        const badgeRow = document.createElement("div");
        badgeRow.className = "badge-row";
        const status = document.createElement("span");
        badge(status, "status", ticket.status);
        const priority = document.createElement("span");
        badge(priority, "priority", ticket.priority);
        badgeRow.append(status, priority);

        const meta = document.createElement("p");
        meta.className = "meta";
        meta.textContent = `Assignee: ${ticket.assignee || "Unassigned"} · ${formatDate(ticket.createdAt)}`;

        button.append(id, title, badgeRow, meta);
        els.ticketList.append(button);
    });
}

async function viewTicketDetails(id) {
    selectedTicketId = id;
    const ticket = await apiFetch(`${API_BASE}/${id}`);
    renderDetails(ticket);
    highlightSelectedCard();
}

function highlightSelectedCard() {
    els.ticketList.querySelectorAll(".ticket-card").forEach((card) => {
        const idText = card.querySelector(".id")?.textContent || "";
        card.classList.toggle("selected", idText === `#${selectedTicketId}`);
    });
}

function renderDetails(ticket) {
    els.noSelection.hidden = true;
    els.ticketDetails.hidden = false;

    els.detailId.textContent = `Ticket #${ticket.id}`;
    els.detailTitle.textContent = ticket.title;
    badge(els.detailStatus, "status", ticket.status);
    badge(els.detailPriority, "priority", ticket.priority);
    els.detailAssignee.textContent = ticket.assignee || "Unassigned";
    els.detailCreated.textContent = formatDate(ticket.createdAt);
    els.detailCreated.dateTime = ticket.createdAt || "";
    els.detailUpdated.textContent = formatDate(ticket.updatedAt);
    els.detailUpdated.dateTime = ticket.updatedAt || "";
    els.detailDescription.textContent = ticket.description || "";

    els.updateTitle.value = ticket.title || "";
    els.updateDescription.value = ticket.description || "";
    els.updatePriority.value = ticket.priority || "LOW";
    els.updateAssignee.value = ticket.assignee || "";

    renderStatusActions(ticket);
    renderComments(ticket.comments || []);
}

function renderStatusActions(ticket) {
    els.statusActions.replaceChildren();
    const actions = STATUS_ACTIONS[ticket.status] || [];
    if (!actions.length) {
        const note = document.createElement("p");
        note.className = "empty-state";
        note.textContent = "No further status transitions are allowed.";
        els.statusActions.append(note);
        return;
    }
    actions.forEach((action) => {
        const button = document.createElement("button");
        button.type = "button";
        button.textContent = action.label;
        button.addEventListener("click", () => updateStatus(ticket.id, action.status));
        els.statusActions.append(button);
    });
}

function renderComments(comments) {
    els.commentThread.replaceChildren();
    if (!comments.length) {
        const empty = document.createElement("p");
        empty.className = "empty-state";
        empty.textContent = "No comments yet.";
        els.commentThread.append(empty);
        return;
    }
    comments.forEach((comment) => {
        const article = document.createElement("article");
        article.className = "comment";

        const header = document.createElement("header");
        const author = document.createElement("strong");
        author.textContent = comment.author;
        const time = document.createElement("time");
        time.textContent = formatDate(comment.createdAt);
        time.dateTime = comment.createdAt || "";
        header.append(author, time);

        const content = document.createElement("p");
        content.textContent = comment.content;
        article.append(header, content);
        els.commentThread.append(article);
    });
}

async function createTicket() {
    const payload = {
        title: document.getElementById("create-title").value.trim(),
        description: document.getElementById("create-description").value.trim(),
        priority: document.getElementById("create-priority").value,
        assignee: document.getElementById("create-assignee").value.trim() || null
    };
    const created = await apiFetch(API_BASE, {
        method: "POST",
        body: JSON.stringify(payload)
    });
    els.createForm.reset();
    els.createModal.close();
    selectedTicketId = created.id;
    await loadTickets();
    await viewTicketDetails(created.id);
}

async function updateStatus(id, newStatus) {
    await apiFetch(`${API_BASE}/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status: newStatus })
    });
    await loadTickets();
    await viewTicketDetails(id);
}

async function updateTicket(id) {
    const payload = {
        title: els.updateTitle.value.trim(),
        description: els.updateDescription.value.trim(),
        priority: els.updatePriority.value,
        assignee: els.updateAssignee.value.trim() || null
    };
    await apiFetch(`${API_BASE}/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload)
    });
    await loadTickets();
    await viewTicketDetails(id);
}

async function addComment(id) {
    const payload = {
        author: els.commentAuthor.value.trim(),
        content: els.commentContent.value.trim()
    };
    await apiFetch(`${API_BASE}/${id}/comments`, {
        method: "POST",
        body: JSON.stringify(payload)
    });
    els.commentContent.value = "";
    await viewTicketDetails(id);
}

function withErrorGuard(handler) {
    return async (event) => {
        if (event) {
            event.preventDefault();
        }
        try {
            await handler(event);
        } catch {
            // RFC-7807 / HTTP errors are already shown in the banner.
        }
    };
}

els.errorDismiss.addEventListener("click", clearError);
els.searchForm.addEventListener("submit", withErrorGuard(loadTickets));
els.openCreateModal.addEventListener("click", () => els.createModal.showModal());
els.cancelCreate.addEventListener("click", () => els.createModal.close());
els.createForm.addEventListener("submit", withErrorGuard(createTicket));
els.updateForm.addEventListener("submit", withErrorGuard(() => {
    if (selectedTicketId == null) {
        return;
    }
    return updateTicket(selectedTicketId);
}));
els.commentForm.addEventListener("submit", withErrorGuard(() => {
    if (selectedTicketId == null) {
        return;
    }
    return addComment(selectedTicketId);
}));

loadTickets().catch(() => {
    // Banner already populated by apiFetch.
});

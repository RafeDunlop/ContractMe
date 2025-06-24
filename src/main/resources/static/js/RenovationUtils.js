let lastSubmittedSearchTerm = "";
let lastSubmittedTags = [];

function fetchRenovations(viewMode = "cards", resetPage = false) {
    if (resetPage) {
        document.getElementById("pageNumber").value = 1;
    }

    lastSubmittedSearchTerm = document.querySelector("input[name='searchTerm']").value.trim();
    lastSubmittedTags = Array.from(document.querySelectorAll("#hidden-tag-inputs input[name='tagNameList']")).map(input => input.value);
    const element = viewMode === "cards" ? document.getElementById("grid") : document.getElementById('table');
    const container = document.getElementById("elements-container")
    const loading = document.getElementById("loading-message");

    const visibility = document.querySelector("select[name='visibility']")?.value || "all";
    updateHeaderTitle(visibility);

    let pageNumber = parseInt(document.getElementById("pageNumber")?.value, 10);
    const searchTerm = lastSubmittedSearchTerm || "";
    const tagNameList = lastSubmittedTags || [];
    if (isNaN(pageNumber) || pageNumber < 1) pageNumber = 1;
    let cardsPerPage = parseInt(document.getElementById("cardsPerPage")?.value, 10);
    if (isNaN(cardsPerPage)) cardsPerPage = 16;
    const currentUserId = document.getElementById("userId").value;

    const params = new URLSearchParams();
    const userParams = new URLSearchParams();

    if (pageNumber && !isNaN(pageNumber)) {
        params.set("page", pageNumber);
        userParams.set("page", pageNumber);
    }
    if (visibility && visibility !== "all") {
        params.set("visibility", visibility);
        userParams.set("visibility", visibility);
    }
    if (searchTerm && searchTerm.trim() !== "") {
        params.set("searchTerm", searchTerm.trim());
        userParams.set("searchTerm", searchTerm.trim())
    }

    tagNameList.forEach(tag => {
        if (tag && tag.trim() !== "") {
            params.append("tagNameList", tag.trim());
            userParams.append("tagNameList", tag.trim());
        }
    });

    if (!isNaN(cardsPerPage)) {
        params.set("cardsPerPage", cardsPerPage);
    }

    const newUrl = new URL(window.location);
    newUrl.search = userParams.toString();
    window.history.replaceState({}, '', newUrl);
    clearAlerts();

    fetch("/renovations/retrieve?" + params.toString())
        .then(response => response.json())
        .then(data => {
            loading.style.display = "none";
            element.style.display = "grid";
            element.innerHTML = "";

            document.getElementById("totalPages").value = data.totalPages;
            document.getElementById("pageNumber").value = data.number + 1;

            if (data.content.length === 0) {
                container.insertAdjacentHTML('beforeend', `<div class="alert alert-secondary mt-4">No renovations found.</div>`);
                element.style.display = "none";
                document.getElementById("pagination").innerHTML = "";
                return;
            }

            document.getElementById("elements-container").style.display = "block";

            if (viewMode === "cards") {
                renderRecordCards(data, currentUserId, pageNumber);
            } else {
                const csrfToken = document.getElementById("globalCsrfToken")?.value || "";
                renderRecordTable(data, pageNumber, csrfToken);
            }

            createPaginationButtons(viewMode);
        })
        .catch(error => {
            loading.style.display = "none";
            element.style.display = "none";
            container.insertAdjacentHTML('beforeend', `<div class="alert alert-danger mt-4">Failed to load renovations. Please try again.</div>`);
            console.log(error);
        });
}

function fetchRenovation(id, resetPage = false) {
    if (resetPage) {
        document.getElementById("pageNumber").value = 1;
    }

    const element = document.getElementById("grid");
    const container = document.getElementById("elements-container")
    const loading = document.getElementById("loading-message");
    const isOwner = document.getElementById("isOwner");


    let pageNumber = parseInt(document.getElementById("pageNumber")?.value, 10);
    if (isNaN(pageNumber) || pageNumber < 1) pageNumber = 1;
    let cardsPerPage = parseInt(document.getElementById("cardsPerPage")?.value, 10);
    if (isNaN(cardsPerPage)) cardsPerPage = 16;
    let totalPages = parseInt(document.getElementById("totalPages")?.value, 10);
    if (!isNaN(totalPages) && pageNumber > totalPages && totalPages > 0) {
        pageNumber = totalPages;
    }

    const params = new URLSearchParams();

    if (pageNumber && !isNaN(pageNumber)) {
        params.set("page", pageNumber);
    }
    if (!isNaN(cardsPerPage)) {
        params.set("cardsPerPage", cardsPerPage);
    }

    fetch("/renovations/retrieve/" + id + "?" + params.toString())
        .then(response => response.json())
        .then(data => {
            loading.style.display = "none";
            element.style.display = "grid";
            element.innerHTML = "";

            const correctedPageNumber = data.number + 1;
            document.getElementById("totalPages").value = data.totalPages;
            document.getElementById("pageNumber").value = correctedPageNumber;

            if (data.content.length === 0) {
                element.style.display = "none";
                document.getElementById("pagination").innerHTML = "";
                return;
            }

            document.getElementById("elements-container").style.display = "block";

            renderTaskCards(data, isOwner, id);
            createPaginationButtons("cards", id);
        })
        .catch(error => {
            loading.style.display = "none";
            element.style.display = "none";
            container.insertAdjacentHTML('beforeend', `<div class="alert alert-danger mt-4">Failed to load renovations. Please try again.</div>`);
            console.log(error);
        });
}

function updateHeaderTitle(visibility) {
    const header = document.getElementById("header-title");
    if (!header) return;

    switch (visibility) {
        case "public":
            header.textContent = "Public Renovation Records";
            break;
        case "user":
            header.textContent = "Your Renovation Records";
            break;
        default:
            header.textContent = "Renovation Records";
    }
}

function renderRecordCards(data, currentUserId, pageNumber) {
    const grid = document.getElementById("grid");
    grid.innerHTML = "";
    grid.className = "grid-container";

    data.content.forEach(record => {
        const tagsHtml = record.sortedTags.map(tag => `
            <span class="tag-box badge bg-success d-flex align-items-center me-2 mb-2">
                <span class="tag-text-small text-truncate">${tag.tagName}</span>
            </span>
        `).join("");

        const card = document.createElement("div");
        card.className = "card card-count position-relative";

        card.innerHTML = `
            <a href="/renovations/view?id=${record.id}&page=${pageNumber}" class="no-underline text-reset">
                ${(record.userId === currentUserId) ? '<span class="badge bg-primary position-absolute top-0 end-0 m-2">Yours</span>' : ""}
                <div class="card-body">
                    <h5 class="card-title truncate">${record.name}</h5>
                    <div class="d-flex flex-wrap">${tagsHtml}</div>
                    <p class="card-text truncate">${record.description}</p>
                    <p class="card-text">
                        ${record.public ? `<span class="badge bg-success">Public</span>` : `<span class="badge bg-secondary">Private</span>`}
                    </p>
                </div>
            </a>
        `;

        grid.appendChild(card);
    });
}

function renderRecordTable(data, pageNumber, csrfToken) {
    const table = document.getElementById("table");
    if (!table) {
        console.error("Table container not found.");
        return;
    }

    table.innerHTML = "";

    data.content.forEach(record => {
        const rowHtml = `
            <a href="/renovations/view?id=${record.id}&page=${pageNumber}" class="list-group-item p-3 mb-3 shadow-sm rounded bg-white position-relative">
                <div class="d-flex justify-content-between align-items-start">
                    <div class="w-100" onclick="document.getElementById('form-${record.id}').submit();" style="cursor: pointer;">
                        <h5 class="mb-1 text-primary">${record.name}</h5>
                        <p class="mb-0 text-muted">${record.description}</p>
                    </div>
                    <button type="button" class="btn btn-outline-danger custom-light-border ms-3"
                        data-id="${record.id}" data-searchQuery="${lastSubmittedSearchTerm}"
                        data-csrf="${csrfToken}"
                        onclick="event.stopPropagation(); confirmDelete(this);">
                        ❌
                    </button>
                </div>
            </a>
        `;
        table.insertAdjacentHTML("beforeend", rowHtml);
    });
}

function renderTaskCards(data, isOwner, renovationId) {
    window.loadedTasks = data.content; // Store for modal rendering
    const grid = document.getElementById("grid");
    grid.innerHTML = "";
    grid.className = "task-grid";

    data.content.forEach(task => {
        const isDefaultIcon = task.iconFileName === 'default-icon.png';

        const iconHtml = `
            <div class="position-relative">
                <img src="/images/${task.iconFileName}" alt="Task Icon" class="task-icon"
                     ${isOwner && !isDefaultIcon ? `onclick="showIconSelector(${task.id})"` : ""} />
                ${isOwner && isDefaultIcon ? `
                    <button type="button" class="btn btn-secondary btn-sm rounded-circle opacity-75 top-0 start-100 translate-middle position-absolute"
                            onclick="showIconSelector(${task.id})">+</button>
                ` : ""}
            </div>
        `;

        const editButton = isOwner ? `
            <a href="/editTask?taskId=${task.id}&renovationId=${renovationId}" class="btn btn-primary">Edit Task</a>
        ` : "";

        const cardHtml = `
            <div class="card card-count">
                <div class="card-body">
                    <div class="d-flex align-items-center">
                        ${iconHtml}
                        <h5 class="card-title truncate ms-2 mb-0">${task.name}</h5>
                    </div>
                    <p class="card-text truncate">${task.description}</p>
                    <p class="card-text"><strong>Due Date:</strong> ${task.dueDate}</p>
                    <div class="d-flex justify-content-between">${editButton}</div>
                </div>
            </div>
        `;

        const wrapper = document.createElement("div");
        wrapper.className = "card-count";
        wrapper.innerHTML = cardHtml;
        grid.appendChild(wrapper);
    });
}

function renderModalContent(task, csrfToken) {
    return `
        <div class="d-flex justify-content-center align-items-center vh-100">
            <div class="card p-4 shadow">
                <h4>Select Task Icon</h4>
                <div class="d-flex flex-row justify-content-start flex-wrap center">
                    ${allIcons.map(icon => `
                        <button class="icon-btn task-icon-button-colour"
                                id="${icon}"
                                type="button"
                                data-taskid="${task.id}"
                                data-csrf="${csrfToken}"
                                onclick="addTaskIcon(this)">
                            <img src="/images/${icon}"
                                 class="img-fluid rounded-circle"
                                 style="width: 100px; height: 100px; object-fit: cover"
                                 alt="Task Icon">
                        </button>
                    `).join('')}
                </div>
                <div class="d-flex flex-row justify-content-start flex-wrap center">
                    <button type="button" class="submit-button btn btn-primary m-2" onclick="submitIcon(${task.id})">Confirm</button>
                    <button type="button" class="delete-button btn btn-secondary m-2"
                            data-taskid="${task.id}" data-csrf="${csrfToken}"
                            onclick="deleteIcon(this)">Delete</button>
                </div>
            </div>
        </div>
    `;
}

function clearAlerts() {
    const oldAlerts = document.querySelectorAll("#elements-container .alert");
    oldAlerts.forEach(alert => alert.remove());
}

function fetchAppropriateRenovationData(viewMode = "cards", id = null, resetPage = false) {
    if (id) {
        fetchRenovation(id, resetPage);
    } else {
        fetchRenovations(viewMode, resetPage);
    }
}

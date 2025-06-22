let lastSubmittedSearchTerm = "";
let lastSubmittedTags = [];

function fetchCards(viewMode = "cards", resetPage = false) {
    if (resetPage) {
        document.getElementById("pageNumberInput").value = 1;
    }

    lastSubmittedSearchTerm = document.querySelector("input[name='searchTerm']").value.trim();
    lastSubmittedTags = Array.from(document.querySelectorAll("#hidden-tag-inputs input[name='tagNameList']")).map(input => input.value);
    console.log(viewMode);
    const element = viewMode === "cards" ? document.getElementById("grid") : document.getElementById('table');
    const container = document.getElementById("elements-container")
    const loading = document.getElementById("loading-message");

    const visibility = document.querySelector("select[name='visibility']")?.value || "all";
    updateHeaderTitle(visibility);

    let pageNumber = parseInt(document.getElementById("pageNumberInput")?.value, 10);
    const searchTerm = lastSubmittedSearchTerm || "";
    const tagNameList = lastSubmittedTags || [];
    if (isNaN(pageNumber) || pageNumber < 1) pageNumber = 1;
    let cardsPerPage = parseInt(document.getElementById("cardsPerPageInput")?.value, 10);
    if (isNaN(cardsPerPage)) cardsPerPage = 16;
    const currentUserEmail = document.getElementById("email").value;

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

    fetch("/renovations/cards?" + params.toString())
        .then(response => response.json())
        .then(data => {
            loading.style.display = "none";
            element.style.display = "grid";
            element.innerHTML = "";

            document.getElementById("data-total-pages").value = data.totalPages;
            document.getElementById("pageNumberInput").value = data.number + 1;

            if (data.content.length === 0) {
                container.insertAdjacentHTML('beforeend', `<div class="alert alert-secondary mt-4">No renovations found.</div>`);
                element.style.display = "none";
                document.getElementById("pagination").innerHTML = "";
                return;
            }

            document.getElementById("elements-container").style.display = "block";

            if (viewMode === "cards") {
                renderCardView(data, currentUserEmail, pageNumber);
            } else {
                const csrfToken = document.getElementById("globalCsrfToken")?.value || "";
                renderTableView(data, csrfToken);
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

function renderCardView(data, currentUserEmail, pageNumber) {
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
            <a href="/renovations/view?id=${record.id}&page=${pageNumber}&fromSearch=true" class="no-underline text-reset">
                ${(record.user.email === currentUserEmail) ? '<span class="badge bg-primary position-absolute top-0 end-0 m-2">Yours</span>' : ""}
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

function renderTableView(data, csrfToken) {
    const table = document.getElementById("table");
    if (!table) {
        console.error("Table container not found.");
        return;
    }

    table.innerHTML = "";

    data.content.forEach(record => {
        const rowHtml = `
            <div class="list-group-item p-3 mb-3 shadow-sm rounded bg-white position-relative">
                <form id="form-${record.id}" method="post" action="/renovations/view">
                    <input type="hidden" name="id" value="${record.id}" />
                    <input type="hidden" name="page" value="1" />
                    <input type="hidden" name="cardsPerPage" value="5" />
                    <input type="hidden" name="fromSearch" value="false" />
                    <input type="hidden" name="_csrf" value="${csrfToken}" />
                </form>
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
            </div>
        `;
        table.insertAdjacentHTML("beforeend", rowHtml);
    });
}

function clearAlerts() {
    const oldAlerts = document.querySelectorAll("#elements-container .alert");
    oldAlerts.forEach(alert => alert.remove());
}

/**
 * Used in viewRenovation.html and renovationSearchTemplate.html
 * Requires confirmPrompt.js loaded beforehand
 */

/**
 * Onclick function for the task search button on View Renovation
 * Uses the confirmation prompt before searching
 * Validates the desired page to visit by checking it's a number and within the bounds of all pages
 * @param recordId the id of renovation being viewed, used to make the url for a successful search
 */

function validateTaskPageSearch(recordId) {
    let desiredPage = document.getElementById("pageSearch").value;
    desiredPage = parseInt(desiredPage, 10);

    const totalPages = parseInt(document.getElementById('totalPages').value, 10);
    const confirmText= "Are you sure you want to go to page " + desiredPage.toString() + "?";
    confirmPrompt(confirmText, "Confirm", "Cancel", false).then((confirm) => {
        if (confirm) {
            if (!isNaN(desiredPage) && desiredPage >= 1 && desiredPage <= totalPages) {
                let url = new URL(window.location.href);
                url.searchParams.set("page", desiredPage);
                url.searchParams.set("id", recordId);
                window.location.href = url.toString();
            } else {
                const errorText = "The page number is outside the range of available pages.";
                document.getElementById("errorMessage").style.display = "block";
                document.getElementById("errorText").innerText = errorText;
            }
        }
    });
}

/**
 * Onclick function for the task search button on View Renovation
 * Uses the confirmation prompt before searching
 * Validates the desired page to visit by checking it's a number and within the bounds of all pages
 */

function validateRenovationPageSearch() {
    let desiredPage = document.getElementById("pageSearch").value;
    desiredPage = parseInt(desiredPage, 10);

    const totalPages = parseInt(document.getElementById('totalPages').value, 10);
    const confirmText= "Are you sure you want to go to page " + desiredPage.toString() + "?";
    confirmPrompt(confirmText, "Confirm", "Cancel", false).then((confirm) => {
        if (confirm) {
            if (!isNaN(desiredPage) && desiredPage >= 1 && desiredPage <= totalPages) {
                let url = new URL(window.location.href);
                url.searchParams.set("page", desiredPage);
                window.location.href = url.toString();
            } else {
                const errorText = "The page number is outside the range of available pages.";
                document.getElementById("errorMessage").style.display = "block";
                document.getElementById("errorText").innerText = errorText;
            }
        }
    });
}

let cachedCardSize = null;

/**
 * This function updates the layout based on the window size and adjusts the number of tasks to be displayed on the page.
 */
function updateLayout(viewMode = "cards", id = null) {
    const cardWidth = 260;
    let cardHeight = 140;

    const containerHeight = window.innerHeight;
    const containerMargin =  parseFloat(getComputedStyle(document.getElementById('container')).marginTop);

    const gridContainer = document.getElementById("elements-container");
    if (gridContainer.style.display === "none") {
        gridContainer.style.display = "block";
    }
    const containerWidth = viewMode === "cards" ?
        document.getElementById('grid').offsetWidth :
        document.getElementById('table');
    const contentHeaderHeight = document.getElementById('content-header').offsetHeight;
    const footerHeight = document.getElementById('footer').offsetHeight;
    let columns = Math.max(1, Math.floor(containerWidth / (cardWidth + 15)));

    if (id) {
        cardHeight = 193;
    } else if (viewMode !== "cards") {
        columns = 1;
        cardHeight = 100;
    }

    const navbarHeight = document.getElementById('navbar').offsetHeight;

    const availableHeight = containerHeight - navbarHeight - containerMargin - contentHeaderHeight - footerHeight - 120;
    const rows = Math.max(1, Math.floor(availableHeight / cardHeight));

    const newCardsPerPage = columns * rows;
    console.log("Container height: " + containerHeight);
    console.log("Container width: " + containerWidth);
    console.log("content header: " + contentHeaderHeight);
    console.log("Footer height: " + footerHeight);
    console.log("Navbar height: " + navbarHeight);
    console.log("Available height: " + availableHeight);

    console.log("Card width: " + cardWidth + " Card Height: " + cardHeight);
    console.log("Rows " + rows + " Columns " + columns);

    // Only update if cardsPerPage changes
    const input = document.getElementById('cardsPerPage');
    const oldValue = parseInt(input.value, 10);

    if (oldValue !== newCardsPerPage && newCardsPerPage > 0) {
        input.value = newCardsPerPage;
        fetchAppropriateRenovationData(viewMode, id, false);
    }
}

function createPaginationButtons(viewMode = "cards", id = null) {
    const pagination = document.getElementById('pagination');
    if (!pagination) return;

    pagination.innerHTML = '';
    const totalPages = parseInt(document.getElementById('totalPages').value, 10);
    const pageNumber = parseInt(document.getElementById("pageNumber").value, 10);
    const paginationLinksStart = Math.max(pageNumber - 2, 1);
    const paginationLinksEnd = Math.min(pageNumber + 2, totalPages);

    if (totalPages <= 10) {
        if (pageNumber > 1) {
            pagination.innerHTML += `<li class="page-item"><button class="page-link" onclick="navigateToPage(${pageNumber - 1}, '${viewMode}', ${id})">Prev</button></li>`;
        }
        for (let i = 1; i <= totalPages; i++) {
            pagination.innerHTML += `<li class="page-item${i === pageNumber ? ' active' : ''}"><button class="page-link" onclick="navigateToPage(${i}, '${viewMode}', ${id})">${i}</button></li>`;
        }
        if (pageNumber < totalPages) {
            pagination.innerHTML += `<li class="page-item"><button class="page-link" onclick="navigateToPage(${pageNumber + 1}, '${viewMode}', ${id})">Next</button></li>`;
        }
    } else {
        pagination.innerHTML += `<li class="page-item"><button class="page-link" onclick="navigateToPage(1, '${viewMode}', ${id})">First</button></li>`;
        pagination.innerHTML += `<li class="page-item"><button class="page-link" onclick="navigateToPage(${pageNumber - 1}, '${viewMode}', ${id})">Prev</button></li>`;

        for (let i = paginationLinksStart; i <= paginationLinksEnd; i++) {
            pagination.innerHTML += `<li class="page-item${i === pageNumber ? ' active' : ''}"><button class="page-link" onclick="navigateToPage(${i}, '${viewMode}', ${id})">${i}</button></li>`;
        }

        pagination.innerHTML += `<li class="page-item"><button class="page-link" onclick="navigateToPage(${pageNumber + 1}, '${viewMode}', ${id})">Next</button></li>`;
        pagination.innerHTML += `<li class="page-item"><button class="page-link" onclick="navigateToPage(${totalPages}, '${viewMode}', ${id})">Last</button></li>`;

        pagination.innerHTML += `
            <li class="page-item d-flex align-items-center">
                <input type="number" class="form-control me-2" id="pageSearch" style="min-width: 50px;">
                <button class="btn btn-primary" onclick="validateRenovationPageSearch(${id})">Search</button>
            </li>`;
    }
}

function navigateToPage(pageNum, viewMode = "cards", id = null) {
    document.getElementById("pageNumber").value = pageNum;
    fetchAppropriateRenovationData(viewMode, id, false);
}

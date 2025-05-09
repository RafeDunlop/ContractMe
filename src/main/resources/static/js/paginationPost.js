/**
 * Used in viewRenovation.html
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

    const totalPages = parseInt(document.getElementById('data-total-pages').value, 10);
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

    const totalPages = parseInt(document.getElementById('data-total-pages').value, 10);
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

/**
 * This function updates the layout based on the window size and adjusts the number of tasks to be displayed on the page.
 */
function updateLayout() {
    const totalPages = parseInt(document.getElementById('data-total-pages').value, 10);
    const grid = document.getElementById('grid');
    const cards = grid.querySelectorAll('.card');
    const paginatedRecordsSize = document.getElementById("paginatedRecordsSize");

    if (cards.length === 0) {
        console.warn("No cards found in grid. Skipping layout update.");
        return;
    }

    const card = cards[0];  // Use the first card for size reference
    const cardStyles = window.getComputedStyle(card);

    const gridWidth = grid.clientWidth;
    const cardWidth = card.offsetWidth + parseFloat(cardStyles.marginLeft) + parseFloat(cardStyles.marginRight);

    const columns = Math.max(1, Math.floor(gridWidth / cardWidth));
    const rows = calculateRows();

    const newCardPerPage = columns * rows;
    const currentCardCount = cards.length;

    // 🔍 Debug logs
    console.debug("=== Layout Debug Info ===");
    console.debug("Grid width:", gridWidth);
    console.debug("Card width (incl. margins):", cardWidth);
    console.debug("Columns:", columns);
    console.debug("Rows:", rows);
    console.debug("Calculated cardPerPage:", newCardPerPage);
    console.debug("Current card count in DOM:", currentCardCount);

    if (newCardPerPage !== currentCardCount && newCardPerPage <= currentCardCount) {
        console.info(`Layout change detected: submitting form with cardPerPage = ${newCardPerPage}`);
        const form = document.getElementById('search-form');
        document.getElementById('cardPerPageInput').value = newCardPerPage;
        form.submit();
    } else {
        console.debug("No layout change detected. No form submission needed.");
    }
}


/**
 * This function calculates the number of rows, using the top of task grid minus footer.
 * Returns the number of rows of tasks to be displayed in the grid
 */
function calculateRows() {
    // Getting elements needed for sizing
    const grid = document.getElementById('grid');
    const card = grid.querySelector('.card');
    const footer = document.getElementById('footer');

    // Calculating the height of the task cards
    const cardStyles = window.getComputedStyle(card);
    const cardHeight = card.offsetHeight + parseFloat(cardStyles.marginTop) + parseFloat(cardStyles.marginBottom);

    // Calculating where the grid of tasks starts relative to page height. Then taking away the footer to get available space
    const gridTop = grid.getBoundingClientRect().top;
    const footerHeight = footer.offsetHeight;
    const pageHeight = window.innerHeight;

    const availableHeight = (pageHeight - gridTop) - footerHeight;

    return Math.max(1, Math.floor(availableHeight / cardHeight));
}

updateLayout()
window.addEventListener('resize', updateLayout);



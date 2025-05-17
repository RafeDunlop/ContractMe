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
    const grid = document.getElementById('grid');
    const cards = grid.querySelectorAll('.card-count');
    //const cards = grid.querySelectorAll('.task-card');
    const totalCards = parseInt(document.getElementById("totalCards").value, 10);
    let pageNumber = parseInt(document.getElementById('pageInput').value, 10);

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

    let newCardsPerPage = columns * rows;
    const currentCardCount = cards.length;
    const finalPage = pageNumber === Math.ceil(totalCards / newCardsPerPage);

    const layoutChanged = newCardsPerPage !== currentCardCount;
    const needsUpdate = layoutChanged && !finalPage;

    if (pageNumber > (totalCards / newCardsPerPage))
    {
        document.getElementById('pageInput').value = Math.ceil(totalCards / newCardsPerPage);
    }

    if (needsUpdate && newCardsPerPage > 0) {
        console.info(`Layout change detected: submitting form with cardsPerPage = ${newCardsPerPage}`);
        const form = document.getElementById('search-form');
        document.getElementById("cardsPerPageInput").value = newCardsPerPage;

        const backendError = document.getElementById('tag-backend-error');
        if (backendError && backendError.hidden === false) {
            backendError.querySelectorAll('li').forEach(li => {
                const text = li.innerText.trim();
                if (!text) {
                    return;
                }
                const errorInput = document.createElement('input');
                errorInput.type = 'hidden';
                errorInput.name = 'errorMessage';
                errorInput.value = text;
                form.appendChild(errorInput);
            });
        }
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

    const availableHeight = (pageHeight - gridTop) - footerHeight - 220;

    return Math.max(1, Math.floor(availableHeight / cardHeight));
}

updateLayout()
window.addEventListener('resize', updateLayout);
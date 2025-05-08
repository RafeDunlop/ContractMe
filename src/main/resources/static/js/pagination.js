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
    console.log("TOTAL PAGES UPDATE: " + totalPages)


    // Getting elements needed for sizing
    const grid = document.getElementById('grid');
    const card = grid.querySelector('.card');
    const cardStyles = window.getComputedStyle(card);

    // Grid and card width used for calculating columns
    const gridWidth = grid.clientWidth;
    const cardWidth = card.offsetWidth + parseFloat(cardStyles.marginTop) + parseFloat(cardStyles.marginBottom);

    // Calculate columns and rows based on available space
    const columns = Math.max(1, Math.floor(gridWidth / cardWidth));  // Number of columns based on grid width
    const rows = calculateRows()

    const cardPerPage = columns * rows;

    // Get the current tasksPerPage from the URL
    const currentParam = new URL(window.location.href).searchParams.get("cardPerPage");

    // Only update the URL if tasksPerPage is different
    if (String(cardPerPage) !== currentParam) {
        let url = new URL(window.location.href);
        url.searchParams.set("cardPerPage", cardPerPage);
        window.location.assign(url.toString());

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



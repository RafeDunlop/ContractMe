/**
 * Used in viewRenovation.html
 * Requires confirmPrompt.js loaded beforehand
 */

const totalPages = parseInt(document.getElementById('data-total-pages').value, 10);

/**
 * Onclick function for the task search button on View Renovation
 * Uses the confirmation prompt before searching
 * Validates the desired page to visit by checking it's a number and within the bounds of all pages
 * @param recordId the id of renovation being viewed, used to make the url for a successful search
 */
function validatePageSearch(recordId) {
    let desiredPage = parseInt(document.getElementById("pageSearch").value, 10);

    const confirmText= "Are you sure you want to go to page " + desiredPage.toString() + "?";
    confirmPrompt(confirmText, "Confirm", "Cancel", false).then((confirm) => {
        if (confirm) {
            if (!isNaN(desiredPage) && desiredPage >= 1 && desiredPage <= totalPages) {
                window.location.href = "/renovations/view?id=" + recordId + "&page=" + desiredPage;
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
    const taskGrid = document.getElementById('taskGrid');
    const taskCard = taskGrid.querySelector('.task-card');

    const gridWidth = taskGrid.clientWidth;
    const cardStyles = window.getComputedStyle(taskCard);
    const cardWidth = taskCard.offsetWidth + parseFloat(cardStyles.marginLeft) + parseFloat(cardStyles.marginRight);

    // Calculate columns and rows based on available space
    const columns = Math.floor(gridWidth / cardWidth); // Number of columns based on grid width


    // Calculate number of rows that can fit

    const rows = calculateRows()
    console.log("Rows that can fit: " + rows);

    const tasksPerPage = columns * rows;

    // Update the grid with the number of columns based on available space
    taskGrid.style.gridTemplateColumns = `repeat(${columns}, 1fr)`; // Dynamically set columns to fit within available width

    // Get the current tasksPerPage from the URL
    const currentParam = new URL(window.location.href).searchParams.get("tasksPerPage");

    // Only update the URL if tasksPerPage is different
    if (String(tasksPerPage) !== currentParam) {
        let url = new URL(window.location.href);
        url.searchParams.set("tasksPerPage", tasksPerPage);
        window.location.assign(url.toString()); // Refresh page with updated tasksPerPage value
    }
}

function calculateRows() {
    const taskGrid = document.getElementById('taskGrid');
    const taskCard = taskGrid.querySelector('.task-card');
    const footer = document.getElementById('footer');

    const cardStyles = window.getComputedStyle(taskCard);
    const cardHeight = taskCard.offsetHeight + parseFloat(cardStyles.marginTop) + parseFloat(cardStyles.marginBottom);

    const gridTop = taskGrid.getBoundingClientRect().top;
    const footerHeight = footer.offsetHeight;
    const pageHeight = window.innerHeight;

    const availableHeight = (pageHeight - gridTop) - footerHeight;

    const rows = Math.max(1, Math.floor(availableHeight / cardHeight));
    console.log("Rows that can fit:", rows);

    return rows;
}

updateLayout()

window.addEventListener('resize', updateLayout);
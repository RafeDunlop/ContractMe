/**
 * Used in viewRenovation.html
 * Requires confirmPrompt.js loaded beforehand
 */

/**
 * This function updates the layout based on the window size and adjusts the number of tasks to be displayed on the page.
 */
function updateLayout() {
    const totalPages = parseInt(document.getElementById('data-total-pages').value, 10);
    console.log("TOTAL PAGES UPDATE: " + totalPages)


    // Getting elements needed for sizing
    const taskGrid = document.getElementById('taskGrid');
    const taskCard = taskGrid.querySelector('.card');
    const cardStyles = window.getComputedStyle(taskCard);

    // Grid and card width used for calculating columns
    const gridWidth = taskGrid.clientWidth;
    const cardWidth = taskCard.offsetWidth + parseFloat(cardStyles.marginTop) + parseFloat(cardStyles.marginBottom);

    // Calculate columns and rows based on available space
    const columns = Math.max(1, Math.floor(gridWidth / cardWidth));  // Number of columns based on grid width
    const rows = calculateRows()

    const itemsPerPage = columns * rows;

    // Get the current tasksPerPage from the URL
    const currentParam = new URL(window.location.href).searchParams.get("itemsPerPage");

    // Only update the URL if tasksPerPage is different
    if (String(itemsPerPage) !== currentParam) {
        let url = new URL(window.location.href);
        url.searchParams.set("itemsPerPage", itemsPerPage);
        window.location.assign(url.toString());

    }
}

/**
 * This function calculates the number of rows, using the top of task grid minus footer.
 * Returns the number of rows of tasks to be displayed in the grid
 */
function calculateRows() {
    // Getting elements needed for sizing
    const taskGrid = document.getElementById('taskGrid');
    const taskCard = taskGrid.querySelector('.task-card');
    const footer = document.getElementById('footer');

    // Calculating the height of the task cards
    const cardStyles = window.getComputedStyle(taskCard);
    const cardHeight = taskCard.offsetHeight + parseFloat(cardStyles.marginTop) + parseFloat(cardStyles.marginBottom);

    // Calculating where the grid of tasks starts relative to page height. Then taking away the footer to get available space
    const gridTop = taskGrid.getBoundingClientRect().top;
    const footerHeight = footer.offsetHeight;
    const pageHeight = window.innerHeight;

    const availableHeight = (pageHeight - gridTop) - footerHeight;

    return Math.max(1, Math.floor(availableHeight / cardHeight));
}

updateLayout()

window.addEventListener('resize', updateLayout);



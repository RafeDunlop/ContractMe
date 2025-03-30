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
    let pageWidth = window.innerWidth;
    let currentTasksPerPage = document.getElementById('tasksPerPage').getAttribute('value');
    let pageHeight = window.innerHeight;

    let tasksPerPage;

    if (pageWidth < 576) {
        tasksPerPage = 3; // Phones
        if (pageHeight > 900) {
            tasksPerPage = 7;
        } else if (pageHeight > 600) {
            tasksPerPage = 5;
        }
    } else if (pageWidth < 768) {
        tasksPerPage = 4; // Tablets
        if (pageHeight > 900) {
            tasksPerPage = 12;
        } else if (pageHeight > 750) {
            tasksPerPage = 8;
        }
    } else if (pageWidth < 992) {
        tasksPerPage = 3; // Small desktop
        if (pageHeight > 1250) {
            tasksPerPage = 12;
        } else if (pageHeight > 1000) {
            tasksPerPage = 9;
        }
        else if (pageHeight > 700) {
            tasksPerPage = 6;
        }
    } else if (pageWidth < 1200) {
        tasksPerPage = 4; // Small desktop
        if (pageHeight > 1000) {
            tasksPerPage = 12;
        } else if (pageHeight > 900) {
            tasksPerPage = 8;
        } else if (pageHeight > 600) {
            tasksPerPage = 4;
        }
    } else if (pageWidth < 1400) {
        tasksPerPage = 5; // desktop
        if (pageHeight > 1200) {
            tasksPerPage = 15;
        } else if (pageHeight > 900) {
            tasksPerPage = 10;
        } else if (pageHeight > 600) {
            tasksPerPage = 5;
        }
    } else {
        tasksPerPage = 5; // large desktop
        if (pageHeight > 1275) {
            tasksPerPage = 20;
        } else if (pageHeight > 1000) {
            tasksPerPage = 15;
        } else if (pageHeight > 900) {
            tasksPerPage = 10;
        }
    }

    if (currentTasksPerPage !== String(tasksPerPage)) {
        let url = new URL(window.location.href);

        // Only update the URL if the tasksPerPage parameter is different
        if (url.searchParams.get('tasksPerPage') !== String(tasksPerPage)) {
            url.searchParams.set('tasksPerPage', tasksPerPage);
            window.location.assign(url.toString()); // Redirect to the new URL with updated tasksPerPage
        }
    }
}

// Initial layout update
updateLayout();

// Listen for window resize and update the layout
window.addEventListener('resize', updateLayout);
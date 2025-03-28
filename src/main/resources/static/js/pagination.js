/**
 * Used in viewRenovation.html
 * Requires confirmPrompt.js loaded beforehand
 */

const totalPages = parseInt(document.getElementById('data-total-pages').value, 10);

/**
 * Onclick method for the task search button on View Renovation
 * Uses the confirmation prompt before searching
 * Validates the desired page to visit by checking it's a number and within the bounds of all pages
 * @param recordId the id of renovation being viewed, used to make the url for a successful search
 */
function validatePageSearch(recordId) {
    let desiredPage = parseInt(document.getElementById("pageSearch").value, 10);

    if (!isNaN(desiredPage) && desiredPage >= 1 && desiredPage <= totalPages) {
        const confirmText= "Are you sure you want to go to page " + desiredPage + "?";
        confirmPrompt(confirmText, "Confirm", "Cancel", false).then((confirm) => {
            if (confirm) {
                window.location.href = "/renovations/view?id=" + recordId + "&page=" + desiredPage;
            }
        });
    } else {
        const errorText = "The page number is outside the range of available pages.";
        document.getElementById("errorMessage").style.display = "block";
        document.getElementById("errorText").innerText = errorText;
    }
}
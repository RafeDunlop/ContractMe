const totalPages = parseInt(document.getElementById('data-total-pages').value, 10);
function validatePageSearch(recordId) {

    let desiredPage = parseInt(document.getElementById("pageSearch").value, 10);
    if (!isNaN(desiredPage) && desiredPage >= 0 && desiredPage <= totalPages - 1) {
        window.location.href = "/renovations/view?id=" + recordId + "&page=" + desiredPage;
    } else {
        const errorText = "The page number is outside the range of available pages.";
        document.getElementById("errorMessage").style.display = "block";
        document.getElementById("errorText").innerText = errorText;
    }
}
const totalPages = document.getElementById('data-total-pages').value;
function validatePageSearch() {
    let desiredPage = document.getElementById("pageSearch").value

    if (desiredPage >= 0 && desiredPage <= totalPages) {
        window.location.href = "/renovations/view?id=" + "PLACEHOLDER ID" + "&page=" + desiredPage;
    } else {
        // ERROR MSG
    }

}
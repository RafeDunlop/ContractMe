function validatePageSearch(_id) {
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

function updateTable() {
    const tableRow = document.getElementsByClassName('table-row')[0];
    const tableRowHeight = tableRow.offsetHeight;
    const footer = document.getElementById('footer');
    const footerHeight = footer.offsetHeight;
    const pageHeight = window.innerHeight;
    const availableHeight = (pageHeight - tableRow.getBoundingClientRect().top) - footerHeight;
    const rows = Math.max(1, Math.floor(availableHeight / tableRowHeight));
    const itemsPerPage = rows;

    const currentParam = new URL(window.location.href).searchParams.get("itemsPerPage");

    if (String(itemsPerPage) !== currentParam) {
        let url = new URL(window.location.href);
        url.searchParams.set("itemsPerPage", itemsPerPage);
        window.location.assign(url.toString());
    }
}

window.addEventListener('resize', updateTable);

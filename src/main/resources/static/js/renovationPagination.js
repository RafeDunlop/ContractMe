function updateTable() {
    const tableRow = document.getElementsByClassName('table-row')[0];
    const tableRowHeight = tableRow.offsetHeight;
    const pageHeight = window.innerHeight;
    const availableHeight = (pageHeight - tableRow.getBoundingClientRect().top);
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

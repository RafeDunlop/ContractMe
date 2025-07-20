/**
 * Updates the calendar view for a renovation record by fetching new calendar fragment
 * for the specified month and year, and replacing the existing calendar.
 *
 * @param id - The ID of the renovation record.
 * @param month - The new month to display
 * @param year - The new year to display
 */
window.changeMonth = function(id, month, year) {
    const page = document.getElementById("pageNumber")?.value || "1";
    const cardsPerPage = document.getElementById("cardsPerPage")?.value || "16";
    const params = new URLSearchParams({ id, page, cardsPerPage, month, year });

    fetch(`${basePath}renovations/calendar?${params.toString()}`)
        .then(r => r.text())
        .then(html => {
            const container = document.getElementById(`calendar-${id}`);
            if (!container) return;
            const tmp = document.createElement("div");
            tmp.innerHTML = html;
            const newWrapper = tmp.querySelector(`#calendar-${id}`);
            if (newWrapper) {
                container.replaceWith(newWrapper);
            }
        });
};

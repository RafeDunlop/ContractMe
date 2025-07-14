document.addEventListener("DOMContentLoaded", () => {
    updateLayout('table');
    window.addEventListener('resize', () => {
        clearTimeout(window._resizeTimeout);
        cachedCardSize = null;
        window._resizeTimeout = setTimeout(() => updateLayout('table'), 400);
    });

    lastSubmittedSearchTerm = document.querySelector("input[name='searchTerm']").value.trim();

    document.getElementById("search-form-table").addEventListener("submit", function (e) {
        e.preventDefault();
        fetchRenovations("table", true);
    });
});
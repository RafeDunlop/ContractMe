document.addEventListener("DOMContentLoaded", () => {
    fetchAppropriateRenovationData('table', null, false);
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

    document.addEventListener('click', function (e) {
        const card = e.target.closest('.renovation-card');
        if (card && !e.target.closest('button')) {
            const url = card.dataset.url;
            if (url) window.location.href = url;
        }
    });
});
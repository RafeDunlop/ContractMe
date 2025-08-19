/*<![CDATA[*/
window.initialTags = /*[[${tagList}]]*/ [];
window.contextPath = /*[[@{/}]]*/ '';
/*]]>*/

document.addEventListener("DOMContentLoaded", () => {
    updateLayout();
    window.addEventListener('resize', () => {
        clearTimeout(window._resizeTimeout);
        cachedCardSize = null;
        window._resizeTimeout = setTimeout(updateLayout, 400);
    });

    lastSubmittedSearchTerm = document.querySelector("input[name='searchTerm']").value.trim();
    lastSubmittedTags = Array.from(document.querySelectorAll("#hidden-tag-inputs input[name='tagNameList']")).map(input => input.value);

    document.getElementById("search-form").addEventListener("submit", function (e) {
        e.preventDefault();
        fetchRenovations("cards", true);
    });
});
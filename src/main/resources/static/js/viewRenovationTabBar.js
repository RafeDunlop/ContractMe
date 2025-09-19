const contentMap = new Map([
    ["details-tab-item", "details-content"],
    ["tasks-tab-item", "tasks-content"],
    ["calendar-tab-item", "calendar-content"]
]);

document.querySelectorAll('.nav-link').forEach(btn => {
    btn.addEventListener('click', function () {
        document.querySelector('.nav-link.active')?.classList.remove('active');
        this.classList.add('active');
        document.querySelectorAll('.renovation-content').forEach(content => {
            content.style.display = 'none';
        });
        document.getElementById(contentMap.get(btn.id)).style.removeProperty('display');

        if (btn.id === "tasks-tab-item") {
            let id = document.getElementById("recordId").value;
            updateLayout("cards", id, true);
        }
    });
});
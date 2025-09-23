/**
 * File is responsible for switching tabs on the view renovation page.
 * Keeps a map of tab button IDs to their corresponding content section IDs.
 * When a tab button is clicked removes the "active" class from the currently active tab makes the clicked active
 * Shows only the content section that matches the clicked tab.
 */

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
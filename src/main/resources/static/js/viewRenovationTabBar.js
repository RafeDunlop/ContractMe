const contentMap = new Map([
    ["details-tab-item", "details-content"],
    ["tasks-tab-item", "tasks-content"],
    ["calendar-tab-item", "calendar-content"]
]);

document.querySelectorAll('.nav-link').forEach(btn => {
    btn.addEventListener('click', function () {
        document.querySelector('.nav-link.active')?.classList.remove('active');
        this.classList.add('active');
        document.querySelector('.renovation-content')?.style.display('none');
        document.getElementById(contentMap.get(btn.id)).style.removeProperty('display');
    });
});
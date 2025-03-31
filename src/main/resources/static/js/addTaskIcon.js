function addTaskIcon(icon) {
    const button = document.getElementById(icon);
    const buttons = document.getElementsByClassName("icon-btn");
    for (const b of buttons) {
        b.classList.remove("active");
    }
    button.classList.add("active");
}

function submitIcon() {
    const button = document.querySelector(".icon-btn.active");
    const iconFileName = button.id;
}

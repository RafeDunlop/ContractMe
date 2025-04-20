function addTaskIcon(button) {
    const buttons = document.getElementsByClassName("icon-btn");
    for (const b of buttons) {
        b.classList.remove("active");
    }
    button.classList.add("active");
}

function getIconSelector(renovationTaskId) {
    const modalId = "icon-selector-" + renovationTaskId;
    return document.getElementById(modalId);
}

function showIconSelector(renovationTaskId) {
    const overlay = getIconSelector(renovationTaskId)
    overlay.style.display = 'block';
}

async function submitIcon(taskId) {
    const overlay = getIconSelector(taskId);
    overlay.style.display = 'none';
    const button = document.querySelector(".icon-btn.active");
    const iconFileName = button.id;
    const csrfToken = button.getAttribute("data-csrf");
    taskId = button.getAttribute("data-taskid");
    const response = await fetch(`/editTask/edit-icon/${taskId}`, {
        method: "POST",
        headers: {'X-CSRF-TOKEN': csrfToken, 'Content-Type': 'application/json'},
        body: JSON.stringify({ iconName: iconFileName })
    });
    if (response.ok) {
        window.location.reload();
    }
}

async function deleteIcon(button) {
    const csrfToken = button.getAttribute("data-csrf");
    const taskId = button.getAttribute("data-taskid");
    const overlay = getIconSelector(taskId);
    overlay.style.display = 'none';
    const response = await fetch(`/editTask/edit-icon/${taskId}`, {
        method: "POST",
        headers: {'X-CSRF-TOKEN': csrfToken, 'Content-Type': 'application/json'},
        body: JSON.stringify({ iconName: "default-icon.png" })
    });
    if (response.ok) {
        window.location.reload();
    }
}

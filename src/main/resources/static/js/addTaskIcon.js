/**
 * Sets the specified icon button to active and disables all others
 * @param button the button to be set to active
 */
function addTaskIcon(button) {
    const buttons = document.getElementsByClassName("icon-btn");
    for (const b of buttons) {
        b.classList.remove("active");
    }
    button.classList.add("active");
}

/**
 * Gets the modal corresponding to the renovation task icon selector identified by the specified id
 * @param renovationTaskId The id of the renovation task whose modal is being gotten
 * @returns {HTMLElement} The modal, an icon selector for the specified renovation task
 */
function getIconSelector(renovationTaskId) {
    const modalId = "icon-selector-" + renovationTaskId;
    return document.getElementById(modalId);
}

/**
 * Toggles the modal corresponding to the specified renovation task to be visible
 * @param renovationTaskId The identifier of the renovation task whose icon selector is to be made visible
 */
function showIconSelector(renovationTaskId) {
    localStorage.setItem("selectedTaskId", renovationTaskId);
    const overlay = getIconSelector(renovationTaskId)
    overlay.style.display = 'block';
}

/**
 * Submits a REST request to set the renovation task's icon
 * @param taskId The id of teh task whose icon is being set
 * @returns {Promise<void>} a promise of the request to be awaited
 */
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
        localStorage.removeItem("selectedTaskId");
        window.location.reload();
    }
}

/**
 * Submits a REST request to dissociate the specified renovation task from its icon
 * @param button The button submitting the request which embeds
 * the id of the renovation task from which to remove the icon
 * and the csrf token to authenticate that this request is from the same domain
 * @returns {Promise<void>} a promise of the request to be awaited
 */
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
        localStorage.removeItem("selectedTaskId");
        window.location.reload();
    }
}


function setSelectedTask(taskId) {
    localStorage.setItem("selectedTaskId", taskId);
}

document.addEventListener("DOMContentLoaded", function () {
    const savedTaskId = localStorage.getItem("selectedTaskId");
    if (savedTaskId) {
        showIconSelector(savedTaskId);
    }
});




/**
 * Updates the calendar view for a renovation record by fetching new calendar fragment
 * for the specified month and year, and replacing the existing calendar.
 *
 * @param id - The ID of the renovation record.
 * @param month - The new month to display
 * @param year - The new year to display
 */
function changeMonth(id, month, year) {
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
}

let clickTimer = null;

/**
 * handle single click on task, show task model
 * @param taskElement calendar task object
 */
function handleTaskClick(taskElement) {
    // Delay single-click action so we can detect double-click
    clickTimer = setTimeout(() => {
        showTaskModal(taskElement);
    }, 300); // 300ms for double-click detection
}

/**
 * handle double click on task, navigate user to task edit page
 * @param renovationId the id of the renovation
 * @param taskElement calendar task object
 * @param event the event that fired this handler
 */
function handleTaskDoubleClick(renovationId, taskElement, event) {
    event.stopPropagation();
    // Cancel the single-click modal if double-clicked
    if (clickTimer) {
        clearTimeout(clickTimer);
        clickTimer = null;
    }
    const taskId = taskElement.dataset.taskId
    window.location.assign(`/editTask?taskId=${taskId}&renovationId=${renovationId}`);
}

/**
 * creates task object from click selection and shows task modal with content
 * @param taskElement calendar task object
 */
async function showTaskModal(taskElement) {
    // Chatgpt used for generating regex - Jake
    // PROMPT: please help me Split the Room list
    const roomList = taskElement.dataset.taskRoomlist
        ? taskElement.dataset.taskRoomlist.replace(/^\[|\]$/g, '').split(',').map(s => s.trim())
        : [];

    const state = taskElement.dataset.taskState
        .toLowerCase()
        .split('_')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');


    const task = {
        id: taskElement.dataset.taskId,
        name: taskElement.dataset.taskName,
        description: taskElement.dataset.taskDesc,
        dueDate: taskElement.dataset.taskDate,
        state: state,
        roomList: roomList,
        iconFileName: taskElement.dataset.taskIconfilename
    };

    const content = document.getElementById("task-modal-content");
    content.innerHTML = renderTaskModalContent(task);

    const modalElement = document.getElementById("task-modal");
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}

/**
 * Renders modal content for displaying task details
 * @param task - The task object.
 * @returns string HTML content string for modal.
 */
function renderTaskModalContent(task) {
    return `
        <div class="modal-header" style="gap: 16px">
            <img src="${basePath}images/${task.iconFileName}" alt="Task Icon" class="calendar-icon" />
            <h5 class="modal-title truncate">${task.name}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
            <p class="modal-content-truncate"><strong>Description:</strong> ${task.description}</p>
            <p class="modal-content-truncate"><strong>Due Date:</strong> ${task.dueDate}</p>
            <p class="modal-content-truncate"><strong>Status:</strong> ${task.state}</p>
            <p class="modal-content-truncate"><strong>Rooms:</strong> ${task.roomList ? task.roomList.join(', ') : ''}</p>
        </div>
    `;
}

/**
 * Open the task creation form
 * @param renovationId the id of the renovation
 * @param cell the cell element which was clicked on
 * @param event the event that fired this function
 */
function addTask(renovationId, cell, event) {
    event.stopPropagation();
    const date = cell.dataset.cellDate;
    window.location.assign(`/renovations/view/create?id=${renovationId}&date=${date}`);
}

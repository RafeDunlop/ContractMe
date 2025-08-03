/**
 * creates task object from click selection and shows task modal with content
 * @param taskElement calendar task object
 */
function showTaskModal(taskElement) {
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

    console.log(task);

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


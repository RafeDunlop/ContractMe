function showTaskModal(taskElement, room) {
    // Remove brackets and split by comma
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
        roomList: roomList
    };

    const content = document.getElementById("task-modal-content");
    content.innerHTML = renderTaskModalContent(task);

    const modalElement = document.getElementById("task-modal");
    const modal = new bootstrap.Modal(modalElement);
    modal.show();
}



/**
 * Renders modal content for icon selection for a task.
 * @param task - The task object.
 * @returns string HTML content string for modal.
 */
function renderTaskModalContent(task) {
    return `
        <div class="modal-header">
            <h5 class="modal-title truncate">${task.name}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
            <p class="truncate"><strong>Description:</strong> ${task.description}</p>
            <p class="truncate"><strong>Due Date:</strong> ${task.dueDate}</p>
            <p class="truncate"><strong>Status:</strong> ${task.state}</p>
            <p class="truncate"><strong>Rooms:</strong> ${task.roomList ? task.roomList.join(', ') : ''}</p>
        </div>
    `;
}


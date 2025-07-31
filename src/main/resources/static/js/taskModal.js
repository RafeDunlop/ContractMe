function showTaskModal(taskElement) {
    const task = {
        id: taskElement.dataset.taskId,
        name: taskElement.dataset.taskName,
        description: taskElement.dataset.taskDesc,
        dueDate: taskElement.dataset.taskDate,
        state: taskElement.dataset.taskState,
        roomList: taskElement.dataset.roomList
    };
    console.log(task);

    const modal = document.getElementById("task-modal");
    const content = document.getElementById("task-modal-content");
    content.innerHTML = renderTaskModalContent(task);
    modal.style.display = "block";
}


/**
 * Renders modal content for icon selection for a task.
 * @param task - The task object.
 * @returns string HTML content string for modal.
 */
function renderTaskModalContent(task) {
    return `
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content p-4 shadow">
                <h5 class="modal-title">${task.name}</h5>
                <div class="modal-body">
                    <h4>${task.description}</h4>
                    <h4>${task.dueDate}</h4>
                    <h4>${task.state}</h4>
                    <h4>${task.roomList ? task.roomList.join(', ') : ''}</h4>
                </div>
            </div>
        </div>
    `;
}

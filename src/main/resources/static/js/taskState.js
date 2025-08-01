/**
 * Sends a PATCH request to update the state of a task, and updates the card UI if it's successful.
 * @param {number} taskId - The ID of the task to update.
 * @param {string} newState - The new state selected in the dropdown.
 * @param {HTMLElement} cardSelect - The select element triggering the update, for getting the specific card.
 */
function updateTaskState(taskId, newState, cardSelect) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || "";
    const card = cardSelect.closest('.card');

    const stateColors = {
        "NOT_STARTED": "#6c757d",
        "IN_PROGRESS": "#0d6efd",
        "BLOCKED": "#ffc107",
        "COMPLETED": "#198754",
        "CANCELLED": "#dc3545"
    };

    const newColor = stateColors[newState];

    fetch(`/task/${taskId}/state?state=${newState}`, {
        method: "PATCH",
        headers: {
            'X-CSRF-TOKEN': csrfToken
        }

    }).then(response => {

        if (response.ok && card && newColor) {
            card.style.borderTop = `5px solid ${newColor}`;
        }

    }).catch(error => {
        console.error("Error:", error);
    });
}

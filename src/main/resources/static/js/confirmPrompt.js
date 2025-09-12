/**
 * Opens a popup with the prompt: "Are you sure you want to delete this renovation record?"
 * and options to cancel or delete
 * if the user selects "delete", a delete request is sent for the corresponding renovation record
 * includes the csrf token provided by spring to avoid csrf attacks (required by spring security)
 * @param button the element being clicked, contains data-id, data-searchTerm (optionally null)
 * and data-csrf in a th:attr tag
 */
export function confirmDelete(button) {
    const prompt = "Are you sure you want to delete this renovation record?";
    confirmPrompt(prompt, "Delete", "Cancel", true).then(async (confirm) => {
        if (confirm) {
            const renovationId = button.getAttribute("data-id");
            const searchTerm = button.getAttribute("data-searchTerm");
            const csrfToken = button.getAttribute("data-csrf");

            const response = await fetch(`renovations/delete/${renovationId}`, {
                method: "DELETE",
                headers: {'X-CSRF-TOKEN': csrfToken}
            })

            if (!response.ok) {
                alert("failed to delete renovation")
            }
            window.location.assign(`renovations?searchTerm=${encodeURIComponent(searchTerm || '')}`);
        }
    });
}

/**
 * Used for the team request form.
 * Asks the user for confirmation before submission and lists the skills in the current team request
 * If the user confirms, the team request form is submitted.
 */
function confirmTeamRequest(e) {
    e.preventDefault();

    const form = document.getElementById('create-team-form');

    const skillCount = updateErrorMessageLabels();
    if (skillCount < 1 || skillCount > 5) return;

    const skills = Array.from(
        document.querySelectorAll('.selected-skill-name')
    ).map(el => `• ${el.textContent}`).join('\n');
    console.log(skills);

    const promptText = `Do you want to create this team request?\n\nRoles:\n\n${skills}\n\n`;

    confirmPrompt(promptText, "Confirm", "Cancel", false)
        .then((ok) => { if (ok) form.submit(); });
}

export function confirmLogout() {
    const prompt = "Are you sure you want to log out?";
    confirmPrompt(prompt, "Confirm", "Cancel", true).then(async (confirm) => {
        if (confirm) {
            const response = await fetch(`logout`, {
                method: "GET",
            })

            if (!response.ok) {
                alert("failed to log out")
            }
            window.location.assign("logout");
        }
    });
}

/**
 * returns a promise which provides a boolean. The boolean corresponds to whether the user clicked confirm
 * @param promptText The text to display in the prompt
 * @param confirmText The text to display for the (left) button , which is the confirm button
 * @param cancelText The text to display for the (right) button, which is the cancel button
 * @param confirmButtonIsDanger Whether the primary button should be a red danger button e.g. delete,
 * or a regular blue button
 * @returns {Promise<unknown>} a promise containing a boolean which returns when the user clicks cancel or delete
 */
export function confirmPrompt(promptText, confirmText, cancelText, confirmButtonIsDanger) {
    const confirmButton = document.getElementById("confirmButton");
    const cancelButton = document.getElementById("cancelButton");
    const promptTextField = document.getElementById("promptText");
    const overlay = document.getElementById("overlay");


    overlay.style.display = 'block';
    confirmButton.className = (confirmButtonIsDanger) ? "btn btn-danger m-3 h-5" : "btn btn-primary m-3 h-5"
    promptTextField.innerText = promptText;
    confirmButton.innerText = confirmText;
    cancelButton.innerText = cancelText;

    return new Promise(confirm => {
        confirmButton.onclick = function confirmTrue() {
            overlay.style.display = 'none';
            confirm(true);
        }

        cancelButton.onclick = function confirmFalse() {
            overlay.style.display = 'none';
            confirm(false);
        }
    });
}
window.confirmTeamRequest = confirmTeamRequest;

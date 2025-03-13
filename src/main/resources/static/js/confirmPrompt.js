export function confirmPrompt(promptText, confirmText, cancelText, confirmButtonIsDanger) {
    alert("called");
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
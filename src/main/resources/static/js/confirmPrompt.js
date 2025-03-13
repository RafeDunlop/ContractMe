function confirmDelete(button) {
    const prompt = "Are you sure you want to delete this renovation record?";
    confirmPrompt(prompt, "Delete", "Cancel", true).then(async (confirm) => {
        if (confirm) {
            const renovationId = button.getAttribute("data-id");
            const searchQuery = button.getAttribute("data-searchQuery");
            const csrfToken = button.getAttribute("data-csrf");

            const response = await fetch(`renovations/delete/${renovationId}`, {
                method: "DELETE",
                headers: {'X-CSRF-TOKEN': csrfToken}
            })

            if (!response.ok) {
                alert("failed to delete renovation")
            }
            window.location.assign(`/renovations?searchQuery=${encodeURIComponent(searchQuery || '')}`);
        }
    });
}

function confirmPrompt(promptText, confirmText, cancelText, confirmButtonIsDanger) {
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
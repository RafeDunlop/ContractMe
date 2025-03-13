import {confirmPrompt} from "./confirmPrompt";

function confirmDelete(button) {
    alert("called");
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
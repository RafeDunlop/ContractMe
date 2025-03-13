async function confirmDelete(button) {
    let confirmed = confirm("Are you sure you want to delete this renovation?");

    if (confirmed) {
        const renovationId = button.getAttribute("data-id");
        const searchQuery = button.getAttribute("data-searchQuery");
        const csrfToken = button.getAttribute("data-csrf");

        const response = await fetch(`renovations/delete/${renovationId}`, {
            method: "DELETE",
            headers: { 'X-CSRF-TOKEN': csrfToken}
        })

        if (!response.ok) {
            alert("failed to delete renovation")
        }
        window.location.assign(`/renovations?searchQuery=${encodeURIComponent(searchQuery || '')}`);
    }
}
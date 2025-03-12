function confirmDelete(button) {
    if (confirm("Are you sure you want to delete this renovation?")) {

        let renovationId = button.getAttribute("data-id");
        window.location.href="/delete-renovation?id=" + renovationId
    }
}
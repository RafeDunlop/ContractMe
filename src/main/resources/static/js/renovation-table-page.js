function confirmDelete(button) {
    if (confirm("Are you sure you want to delete this renovation?")) {
        let renovationId = button.getAttribute("data-id");
        let form = document.createElement("form");
        form.method = "POST";
        form.action = "/delete-renovation";

        let input = document.createElement("input");
        input.type = "hidden";
        input.name = "id";
        input.value = renovationId;

        form.appendChild(input);
        document.body.appendChild(form);
        form.submit();
    }
}
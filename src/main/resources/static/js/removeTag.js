/**
 * Removes the tag specified from the renovation
 * @param button The button pressed, contains the data needed for the method call
 * @returns {Promise<void>} A promise to call the PATCH request
 */
async function removeTag(button) {
    const csrfToken = button.getAttribute("data-csrf");
    const renovationId = button.getAttribute("data-renovationId");
    const tag = document.getElementById("tag-input");
    const response = await fetch(`renovations/tags/remove?renovationId=${renovationId}&tagName=${tag}`, {
        method: 'PATCH',
        headers: {'X-CSRF-TOKEN': csrfToken, 'Content-Type': 'application/json'}
    })
    if (response.ok) {
        window.location.reload();
    }
}
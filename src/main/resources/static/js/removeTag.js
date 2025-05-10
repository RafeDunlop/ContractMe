

async function removeTag(tag, renovationId, csrfToken) {
    const response = await fetch('renovations/tags/remove', {
        method: 'PATCH',
        headers: {'X-CSRF-TOKEN': csrfToken, 'Content-Type': 'application/json'},
        body: JSON.stringify({
            renovationId: renovationId,
            tagName: tag
        })
    })
    if (response.ok) {
        window.location.reload();
    }
}
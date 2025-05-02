/**
 * Sends a POST request to update the publicity of a renovation
 * when the checkbox is toggled.
 */
document.getElementById('publicCheckbox').addEventListener('change', async function () {
  const isPublic = this.checked;
  const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
  const renovationId = document.getElementById('renovationId').value;
  const response = await fetch(`editPublicity/${renovationId}`, {
    method: "POST",
    headers: {'X-CSRF-TOKEN': csrfToken, 'Content-Type': 'application/json'},
    body: JSON.stringify({isPublic: isPublic})
  });
});
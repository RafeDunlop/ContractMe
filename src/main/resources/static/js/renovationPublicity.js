/**
 * Sends a POST request to update the publicity of a renovation
 * when the checkbox is toggled.
 */

// Null guard event listener for public records that use same view template but no checkbox for publicity.
const publicCheckbox = document.getElementById('publicCheckbox');

if (publicCheckbox) {
  document.getElementById('publicCheckbox').addEventListener('change', async function () {
    const isPublic = this.checked;
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const renovationId = document.getElementById('recordId').value;
    const response = await fetch(`renovations/editPublicity/${renovationId}`, {
      method: "POST",
      headers: {'X-CSRF-TOKEN': csrfToken, 'Content-Type': 'application/json'},
      body: JSON.stringify({isPublic: isPublic})
    });
  });
}

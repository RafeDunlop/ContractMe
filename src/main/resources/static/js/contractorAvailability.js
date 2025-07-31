/**
 * Sends a POST request to update the availability of a contractor
 * when the checkbox is toggled.
 */

// Null guard event listener for users that use same view template but no checkbox for availability.
const availabilityCheckbox = document.getElementById('availabilityCheckbox');

if (availabilityCheckbox) {
    document.getElementById('availabilityCheckbox').addEventListener('change', async function () {
        const isAvailable = this.checked;
        const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
        const contractorId = document.getElementById("contractor").value;
        const response = await fetch(`/editAvailability/${contractorId}`, {
            method: "POST",
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ isAvailable: isAvailable })
        });
    });
}

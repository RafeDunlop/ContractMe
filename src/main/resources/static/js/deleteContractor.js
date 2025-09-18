const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const teamId = document.getElementById("teamId").value;
const contractorId = document.getElementById("contractorId").value;


/** Sends a delete request to the TeamController to delete a contractor from a role */
function deleteContractor(teamId,contractorId) {
  fetch(`/renovations/team/delete?teamId=${encodeURIComponent(teamId)}&contractorId=${encodeURIComponent(contractorId)}`, {
    method: "DELETE",
    headers: {
      'X-CSRF-TOKEN': csrfToken
    }
  })
  .then(response => {
    if (!response.ok) {
      throw new Error("Delete failed with status " + response.status);
    }
    // Reload the page to show updated team info
    window.location.reload();
  })
  .catch(err => {
    console.error(err);
    alert("Could not remove contractor.");
  });
}

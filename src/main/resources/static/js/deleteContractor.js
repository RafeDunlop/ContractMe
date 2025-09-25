import { confirmPrompt } from "./confirmPrompt.js";

const removeButton = document.getElementById("remove-contractor-button");
if (removeButton != null) {
    removeButton.addEventListener("click", confirmContractorRemove);
}

/**
 * Event listener for the contractor remove button, opens a confirmation
 * prompt to confirm the delete action.
 */
function confirmContractorRemove(event) {
    const contractorId = Number(event.target.dataset.contractorId);
    const promptText = "Are you sure you want to remove this contractor?";
    confirmPrompt(promptText, "Remove", "Cancel", true).then((ok) => {if (ok) deleteContractor(contractorId); });
}

/** Sends a delete request to the TeamController to delete a contractor from a role */
function deleteContractor(contractorId) {
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const teamId = document.getElementById("teamId").value;
  fetch(`renovations/team/delete?teamId=${encodeURIComponent(teamId)}&contractorId=${encodeURIComponent(contractorId)}`, {
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

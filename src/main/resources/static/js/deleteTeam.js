/** Js file used for the delete team confirm prompt, on viewRenovation.html */

import {confirmPrompt} from "./confirmPrompt.js";

const deleteButton  = document.getElementById("delete-team-button");
const containerDiv = document.getElementById("delete-team-div");

/** Prevents default submission of form, and brings up confirm prompt. */
if (deleteButton && containerDiv) {
    deleteButton.addEventListener("click", () =>
        confirmPrompt("Are you sure you want to delete this team?", "Delete", "Cancel", true)
            .then(ok => {
                if (ok) deleteTeam(deleteButton.getAttribute("data-id"), deleteButton.getAttribute("data-csrf"));
            })
    );
}

function deleteTeam(id, csrf) {
    fetch(`renovations/team/delete/${id}`, {
        method: "DELETE",
        headers: {'X-CSRF-TOKEN': csrf}
    }).then(() => window.location.reload())
}
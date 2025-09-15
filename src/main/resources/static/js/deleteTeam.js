/** Js file used for the delete team confirm prompt, on viewRenovation.html */

import {confirmPrompt} from "./confirmPrompt.js";

const deleteButton  = document.getElementById("delete-team-button");
const form = document.getElementById("delete-team-form");

/** Prevents default submission of form, and brings up confirm prompt. */
if (deleteButton && form) {
    deleteButton.addEventListener("click", (e) => {
        e.preventDefault();
        confirmPrompt("Are you sure you want to delete this team?", "Delete", "Cancel", true)
            .then(ok => { if (ok) form.submit(); });
    });
}
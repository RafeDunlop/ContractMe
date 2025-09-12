import {confirmPrompt} from "./confirmPrompt.js";

const deleteButton  = document.getElementById("delete-team-button");
const form = document.getElementById("delete-team-form");

if (deleteButton && form) {
    deleteButton.addEventListener("click", (e) => {
        e.preventDefault();
        confirmPrompt("Are you sure you want to delete this team?", "Delete", "Cancel", true)
            .then(ok => { if (ok) form.submit(); });
    });
}
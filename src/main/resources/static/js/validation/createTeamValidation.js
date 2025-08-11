/**
 * Controls the enabled state of the "Add" button based on skill selection.
 */
document.getElementById('skills-select').addEventListener('change', function() {
    const addButton = document.getElementById('addSkillButton');
    addButton.disabled = this.selectedIndex === 0;
});

/** Used for front end validation for checking amount of skills selected */
function getSkillCount() {
    return document.querySelectorAll('#selected-skills input[name="skills"]').length;
}

/** Update error message labels*/
function updateErrorMessageLabels() {
    const count = getSkillCount();

    let sizeFrontendError = document.getElementById("skill-frontend-error");
    let sizeFrontendErrorMessage = document.getElementById("skill-frontend-error-message");
    let submitButton = document.getElementById("form-submit");

    if (count === 0) {
        sizeFrontendErrorMessage.textContent = "Your team request must have at least one role.";
        sizeFrontendError.hidden = false;
        submitButton.disabled = true;
    } else if (count > 5) {
        sizeFrontendErrorMessage.textContent = "Your team request cannot have more than 5 roles.";
        sizeFrontendError.hidden = false;
        submitButton.disabled = true;
    } else {
        sizeFrontendErrorMessage.textContent = "";
        sizeFrontendError.hidden = true;
        submitButton.disabled = false;
    }
    return count;
}
/** Used for front end validation for checking amount of skills selected */
function getSkillCount() {
    return document.querySelectorAll('.skill-card-container').length;
}

/** Display error message for too many roles */
function displayTooManyRolesError() {
    let sizeFrontendError = document.getElementById("skill-frontend-error");
    let sizeFrontendErrorMessage = document.getElementById("skill-frontend-error-message");
    sizeFrontendErrorMessage.textContent = "Your team request cannot have more than 5 roles.";
    sizeFrontendError.hidden = false;
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
    } else {
        sizeFrontendErrorMessage.textContent = "";
        sizeFrontendError.hidden = true;
        submitButton.disabled = false;
    }
    return count;
}
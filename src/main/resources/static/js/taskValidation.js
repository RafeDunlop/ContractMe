// === Elements ===

// Name
let nameField = document.getElementById("name");
let nameFrontendError = document.getElementById("name-frontend-error");
let nameFrontendErrorMessage = document.getElementById("name-frontend-error-message");
let nameBackendError = document.getElementById("name-backend-error");

// Description
let descriptionField = document.getElementById("description");
let descriptionFrontendError = document.getElementById("description-frontend-error");
let descriptionFrontendErrorMessage = document.getElementById("description-frontend-error-message");
let descriptionBackendError = document.getElementById("description-backend-error");

// Due Date
let dueDateField = document.getElementById("dueDate");
let dueDateFrontendError = document.getElementById("dueDate-frontend-error");
let dueDateFrontendErrorMessage = document.getElementById("dueDate-frontend-error-message");
let dueDateBackendError = document.getElementById("dueDate-backend-error");

// Form
let form = document.getElementById("create-task-form");

// === Patterns ===
const namePattern = /^[\p{L}\d\s\-'.]{1,128}$/u;
const maxDescriptionLength = 512;

// === Event Listeners ===
nameField.addEventListener("input", () => validateName(nameField.value));
descriptionField.addEventListener("input", () => validateDescription(descriptionField.value));
dueDateField.addEventListener("input", () => validateDueDate(dueDateField.value));
dueDateField.addEventListener("change", () => validateDueDate(dueDateField.value));

form.addEventListener("submit", function (e) {
    let valid = true;

    if (!validateName(nameField.value)) valid = false;
    if (!validateDescription(descriptionField.value)) valid = false;
    if (!validateDueDate(dueDateField.value)) valid = false;

    if (!valid) {
        e.preventDefault(); // Prevent form from submitting
    }
});

// === Validation Functions ===

function validateName(input) {
    input = input.trim();
    if (input.length > 128) {
        setError(nameFrontendError, nameFrontendErrorMessage, "Task name cannot be longer than 128 characters");
        nameBackendError.hidden = true;
        return false;
    } else if (input === "" || !namePattern.test(input)) {
        setError(nameFrontendError, nameFrontendErrorMessage, "Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes");
        nameBackendError.hidden = true;
        return false;
    } else {
        clearError(nameFrontendError, nameFrontendErrorMessage);
        nameBackendError.hidden = true;
        return true;
    }
}

function validateDescription(input) {
    input = input.trim();
    if (input === "") {
        setError(descriptionFrontendError, descriptionFrontendErrorMessage, "Task description cannot be empty.");
        descriptionBackendError.hidden = true;
        return false;
    } else if (input.length > maxDescriptionLength) {
        setError(descriptionFrontendError, descriptionFrontendErrorMessage, `Description must be ${maxDescriptionLength} characters or fewer.`);
        descriptionBackendError.hidden = true;
        return false;
    } else {
        clearError(descriptionFrontendError, descriptionFrontendErrorMessage);
        descriptionBackendError.hidden = true;
        return true;
    }
}

function validateDueDate(dateValue) {
    if (dateValue && new Date(dateValue) < new Date()) {
        dueDateFrontendError.hidden = false;
        dueDateFrontendErrorMessage.innerText = "Due date must be in the future.";
        dueDateBackendError.hidden = true; // Hide backend error when frontend validation fails
        return false;
    }

    dueDateFrontendError.hidden = true;
    dueDateBackendError.hidden = true; // Show backend error if set (this is only needed when backend validation has issues)
    return true;
}



// === Utility Functions ===

function setError(container, messageElement, message) {
    messageElement.textContent = message;
    container.hidden = false;
    messageElement.hidden = false;
}

function clearError(container, messageElement) {
    messageElement.textContent = "";
    container.hidden = true;
    messageElement.hidden = true;
}

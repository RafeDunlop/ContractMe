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
    if (nameBackendError) nameBackendError.hidden = true;
    if (input.length > 128) {
        setError(nameFrontendError, nameFrontendErrorMessage, "Task name cannot be greater than 128 characters");
        return false;
    } else if (input === "" || !namePattern.test(input)) {
        setError(nameFrontendError, nameFrontendErrorMessage, "Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes");
        return false;
    } else {
        clearError(nameFrontendError, nameFrontendErrorMessage);
        return true;
    }
}

function validateDescription(input) {
    input = input.trim();
    if (descriptionBackendError) descriptionBackendError.hidden = true;
    if (input === "") {
        setError(descriptionFrontendError, descriptionFrontendErrorMessage, "Task description cannot be empty.");
        return false;
    } else if (input.length > maxDescriptionLength) {
        setError(descriptionFrontendError, descriptionFrontendErrorMessage, `Task description must be ${maxDescriptionLength} characters or less.`);
        return false;
    } else {
        clearError(descriptionFrontendError, descriptionFrontendErrorMessage);
        return true;
    }
}

function validateDueDate(dateValue) {
    if (dueDateBackendError) dueDateBackendError.hidden = true;
    if (dateValue && new Date(dateValue) < new Date()) {
        dueDateFrontendError.hidden = false;
        dueDateFrontendErrorMessage.innerText = "Due date must be in the future.";
        return false;
    }

    dueDateFrontendError.hidden = true;
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

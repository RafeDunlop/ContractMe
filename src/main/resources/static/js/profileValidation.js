import {
    checkEmailField,
    checkNameField,
} from './userValidation.js';

document.addEventListener("DOMContentLoaded", () => {

    // Elements
    let emailField = document.getElementById("email");
    let firstNameField = document.getElementById("first-name");
    let lastNameField = document.getElementById("last-name");

    let emailFrontendError = document.getElementById("email-frontend-error");
    let emailFrontendErrorMessage = document.getElementById("email-frontend-error-message");
    let emailBackendError = document.getElementById("email-backend-error");

    let firstNameFrontendError = document.getElementById("first-name-frontend-error");
    let firstNameFrontendErrorMessage = document.getElementById("first-name-frontend-error-message");
    let firstNameBackendError = document.getElementById("first-name-backend-error");

    let lastNameFrontendError = document.getElementById("last-name-frontend-error");
    let lastNameFrontendErrorMessage = document.getElementById("last-name-frontend-error-message");
    let lastNameBackendError = document.getElementById("last-name-backend-error");

// Event listeners
    emailField.addEventListener("input", () => {
        checkEmailField(
            emailField.value,
            emailFrontendErrorMessage,
            emailFrontendError,
            emailBackendError
        );
    });

    firstNameField.addEventListener("input", () => {
        checkNameField(
            firstNameField.value,
            "First",
            firstNameFrontendError,
            firstNameFrontendErrorMessage,
            firstNameBackendError
        );
    });

    lastNameField.addEventListener("input", () => {
        checkNameField(
            lastNameField.value,
            "Last",
            lastNameFrontendError,
            lastNameFrontendErrorMessage,
            lastNameBackendError
        );
    });

});
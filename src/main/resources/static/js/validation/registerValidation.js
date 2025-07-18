import {
    checkEmailField,
    checkNameField,
    checkPasswordStrength,
    checkPasswordMatch
} from './userValidation.js';

document.addEventListener("DOMContentLoaded", () => {

    // Elements
    let emailField = document.getElementById("email");
    let emailFrontendError = document.getElementById("email-frontend-error");
    let emailFrontendErrorMessage = document.getElementById("email-frontend-error-message");
    let emailBackendError = document.getElementById("email-backend-error");

    let firstNameField = document.getElementById("firstName");
    let lastNameField = document.getElementById("lastName");

    let firstNameFrontendError = document.getElementById("firstName-frontend-error");
    let lastNameFrontendError = document.getElementById("lastName-frontend-error");

    let firstNameFrontendErrorMessage = document.getElementById("firstName-frontend-error-message");
    let lastNameFrontendErrorMessage = document.getElementById("lastName-frontend-error-message");

    let firstNameBackendError = document.getElementById("firstName-backend-error");
    let lastNameBackendError = document.getElementById("lastName-backend-error");

    let passwordField = document.getElementById("password");
    let confirmPasswordField = document.getElementById("confirmPassword");
    let passwordFrontendError = document.getElementById("password-frontend-error");
    let passwordFrontendErrorMessage = document.getElementById("password-frontend-error-message");
    let passwordBackendError = document.getElementById("password-backend-error");
    let confirmPasswordFrontendError = document.getElementById("confirmPassword-frontend-error");
    let confirmPasswordFrontendErrorMessage = document.getElementById("confirmPassword-frontend-error-message");
    let confirmPasswordBackendError = document.getElementById("confirmPassword-backend-error");

    // Event listeners
    emailField.addEventListener("input", () =>
        checkEmailField(emailField.value,
            emailFrontendErrorMessage,
            emailFrontendError,
            emailBackendError)
    );

    firstNameField.addEventListener("input", () => {
        checkNameField(firstNameField.value,
            "First",
            firstNameFrontendError,
            firstNameFrontendErrorMessage,
            firstNameBackendError
        );
    });

    lastNameField.addEventListener("input", () => {
        checkNameField(lastNameField.value,
            "Last",
            lastNameFrontendError,
            lastNameFrontendErrorMessage,
            lastNameBackendError
        );
    });

    passwordField.addEventListener("input", () => {
        checkPasswordStrength(passwordField,
            passwordFrontendError,
            passwordFrontendErrorMessage,
            passwordBackendError
        );
    });

    confirmPasswordField.addEventListener("input", () => {
        checkPasswordMatch(passwordField,
            confirmPasswordField,
            confirmPasswordFrontendError,
            confirmPasswordFrontendErrorMessage,
            confirmPasswordBackendError
        );
    });
});
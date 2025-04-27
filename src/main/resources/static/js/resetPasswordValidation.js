import {
    checkPasswordStrength,
    checkPasswordMatch
} from './userValidation.js';

document.addEventListener("DOMContentLoaded", () => {

    // Elements
    let newPasswordField = document.getElementById("newPassword");
    let confirmNewPasswordField = document.getElementById("confirmNewPassword");
    let newPasswordFrontendError = document.getElementById("new-password-frontend-error");
    let newPasswordFrontendErrorMessage = document.getElementById("new-password-frontend-error-message");
    let newPasswordBackendError = document.getElementById("new-password-backend-error");
    let confirmNewPasswordFrontendError = document.getElementById("confirm-new-password-frontend-error");
    let confirmNewPasswordFrontendErrorMessage = document.getElementById("confirm-new-password-frontend-error-message");
    let confirmNewPasswordBackendError = document.getElementById("confirm-new-password-backend-error");

    // Event listeners
    newPasswordField.addEventListener("input", () => {
        checkPasswordStrength(newPasswordField,
            newPasswordFrontendError,
            newPasswordFrontendErrorMessage,
            newPasswordBackendError
        );
    });

    confirmNewPasswordField.addEventListener("input", () => {
        checkPasswordMatch(newPasswordField,
            confirmNewPasswordField,
            confirmNewPasswordFrontendError,
            confirmNewPasswordFrontendErrorMessage,
            confirmNewPasswordBackendError
        );
    });
});
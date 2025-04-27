import {
    checkPasswordStrength,
    checkPasswordMatch
} from './userValidation.js';

document.addEventListener("DOMContentLoaded", () => {

    // Elements
    let newPasswordField = document.getElementById("new-password")
    let newPasswordFrontendError = document.getElementById("new-password-frontend-error")
    let newPasswordFrontendErrorMessage = document.getElementById("new-password-frontend-error-message")
    let newPasswordBackendError = document.getElementById("new-password-backend-error")

    let newPasswordRetypedField = document.getElementById("new-password-retyped")
    let newPasswordRetypedFrontendError = document.getElementById("new-password-retyped-frontend-error")
    let newPasswordRetypedFrontendErrorMessage = document.getElementById("new-password-retyped-frontend-error-message")
    let newPasswordRetypedBackendError = document.getElementById("new-password-retyped-backend-error")

    // Event listeners
    newPasswordField.addEventListener("input", () => {
        checkPasswordStrength(newPasswordField,
            newPasswordFrontendError,
            newPasswordFrontendErrorMessage,
            newPasswordBackendError
        );
    });

    newPasswordRetypedField.addEventListener("input", () => {
        checkPasswordMatch(newPasswordField,
            newPasswordRetypedField,
            newPasswordRetypedFrontendError,
            newPasswordRetypedFrontendErrorMessage,
            newPasswordRetypedBackendError
        );
    });
});
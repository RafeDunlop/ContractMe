import { checkEmailField } from './validation/userValidation.js';

document.addEventListener("DOMContentLoaded", () => {

    // Elements
    let emailField = document.getElementById("username");
    let emailFrontendError = document.getElementById("email-frontend-error");
    let emailFrontendErrorMessage = document.getElementById("email-frontend-error-message");
    let emailBackendError = document.getElementById("email-backend-error");

    // Event listeners
    emailField.addEventListener("input", () =>
        checkEmailField(emailField.value,
            emailFrontendErrorMessage,
            emailFrontendError,
            emailBackendError)
    );

});
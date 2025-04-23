let newPasswordField = document.getElementById("newPassword");
let confirmNewPasswordField = document.getElementById("confirmNewPassword");
let newPasswordFrontendError = document.getElementById("new-password-frontend-error");
let newPasswordFrontendErrorMessage = document.getElementById("new-password-frontend-error-message");
let newPasswordBackendError = document.getElementById("new-password-backend-error");
let confirmNewPasswordFrontendError = document.getElementById("confirm-new-password-frontend-error");
let confirmNewPasswordFrontendErrorMessage = document.getElementById("confirm-new-password-frontend-error-message");
let confirmNewPasswordBackendError = document.getElementById("confirm-new-password-backend-error");

newPasswordField.addEventListener("input", checkPasswordStrength);
confirmNewPasswordField.addEventListener("input", checkPasswordMatch);


function checkPasswordStrength() {
    const password = newPasswordField.value;

    const strengthError = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";

    let passwordErrors = [];

    if (
        password.length < 8 ||
        !/[A-Z]/.test(password) ||
        !/[a-z]/.test(password) ||
        !/\d/.test(password) ||
        !/[^a-zA-Z0-9]/.test(password)
    ) {
        passwordErrors.push(strengthError);
    }

    if (passwordErrors.length > 0) {
        newPasswordFrontendErrorMessage.textContent = passwordErrors[0];
        newPasswordFrontendError.hidden = false;
        newPasswordFrontendErrorMessage.hidden = false;
        newPasswordBackendError.hidden = true;
    } else {
        newPasswordFrontendErrorMessage.textContent = "";
        newPasswordFrontendError.hidden = true;
        newPasswordFrontendErrorMessage.hidden = true;
        newPasswordBackendError.hidden = true;
    }
}

function checkPasswordMatch() {
    const password = newPasswordField.value;
    const confirmPassword = confirmNewPasswordField.value;

    if (password !== confirmPassword) {
        confirmNewPasswordFrontendErrorMessage.textContent = "Passwords do not match.";
        confirmNewPasswordFrontendError.hidden = false;
        confirmNewPasswordFrontendErrorMessage.hidden = false;
        confirmNewPasswordBackendError.hidden = true;
    } else {
        confirmNewPasswordFrontendErrorMessage.textContent = "";
        confirmNewPasswordFrontendError.hidden = true;
        confirmNewPasswordFrontendErrorMessage.hidden = true;
        confirmNewPasswordBackendError.hidden = true;
    }
}
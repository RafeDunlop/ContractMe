//Elements


let newPasswordField = document.getElementById("new-password")
let newPasswordFrontendError = document.getElementById("new-password-frontend-error")
let newPasswordFrontendErrorMessage = document.getElementById("new-password-frontend-error-message")
let newPasswordBackendError = document.getElementById("new-password-backend-error")

let newPasswordRetypedField = document.getElementById("new-password-retyped")
let newPasswordRetypedFrontendError = document.getElementById("new-password-retyped-frontend-error")
let newPasswordRetypedFrontendErrorMessage = document.getElementById("new-password-retyped-frontend-error-message")
let newPasswordRetypedBackendError = document.getElementById("new-password-retyped-backend-error")


//Event listeners
newPasswordField.addEventListener("input", () => checkPasswordStrength());
newPasswordRetypedField.addEventListener("input", () => checkPasswordMatch());




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
    const confirmPassword = newPasswordRetypedField.value;



    if (password !== confirmPassword) {
        newPasswordRetypedFrontendErrorMessage.textContent = "Passwords do not match.";
        newPasswordRetypedFrontendError.hidden = false;
        newPasswordRetypedFrontendErrorMessage.hidden = false;
        newPasswordRetypedBackendError.hidden = true;
    } else {
        newPasswordRetypedFrontendErrorMessage.textContent = "";
        newPasswordRetypedFrontendError.hidden = true;
        newPasswordRetypedFrontendErrorMessage.hidden = true;
        newPasswordRetypedBackendError.hidden = true;
    }
}

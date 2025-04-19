let emailField = document.getElementById("email");
let emailFrontendError = document.getElementById("email-frontend-error");
let emailFrontendErrorMessage = document.getElementById("email-frontend-error-message");
let emailBackendError = document.getElementById("email-backend-error");

const emailPattern = /^[A-Za-z0-9]+([+_.-][A-Za-z0-9]+)*@[A-Za-z0-9]+([.-][A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
emailField.addEventListener("input", () => checkEmailField(emailField.value));

function checkEmailField(input) {
    input = input.trim();
    if (input === "" || !emailPattern.test(input)) {
        emailFrontendErrorMessage.textContent = "Email address must be in the form ‘jane@doe.nz’.";
        emailFrontendError.hidden = false;
        emailFrontendErrorMessage.hidden = false;
        emailBackendError.hidden = true;
    } else {
        emailFrontendErrorMessage.textContent = "";
        emailFrontendError.hidden = true;
        emailFrontendErrorMessage.hidden = true;
        emailBackendError.hidden = true;
    }
}
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

// Validity flags
let emailValid = false;
let firstNameValid = false;
let lastNameValid = false;
let passwordValid = false;

// Regex patterns
const emailPattern = /^[A-Za-z0-9]+([+_.-][A-Za-z0-9]+)*@[A-Za-z0-9]+([.-][A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
const namePattern = /^[\p{L}\-'\s]*$/u;

// Event listeners
emailField.addEventListener("input", () => checkEmailField(emailField.value));
firstNameField.addEventListener("input", () => checkNameField(firstNameField.value, "First"));
lastNameField.addEventListener("input", () => checkNameField(lastNameField.value, "Last"));
passwordField.addEventListener("input", checkPasswordStrength);
confirmPasswordField.addEventListener("input", checkPasswordMatch);

// Functions

function checkEmailField(input) {
    input = input.trim();
    if (input === "" || !emailPattern.test(input)) {
        emailFrontendErrorMessage.textContent = "Email address must be in the form ‘jane@doe.nz’.";
        emailFrontendError.hidden = false;
        emailFrontendErrorMessage.hidden = false;
        emailBackendError.hidden = true;
        emailValid = false;
    } else {
        emailFrontendErrorMessage.textContent = "";
        emailFrontendError.hidden = true;
        emailFrontendErrorMessage.hidden = true;
        emailBackendError.hidden = true;
        emailValid = true;
    }
}

function checkNameField(input, nameType) {
    const errorElement = nameType === "First" ? firstNameFrontendError : lastNameFrontendError;
    const errorMessageElement = nameType === "First" ? firstNameFrontendErrorMessage : lastNameFrontendErrorMessage;
    const backendErrorElement = nameType === "First" ? firstNameBackendError : lastNameBackendError;

    input = input.trim();

    if (nameType === "First" && input === "") {
        errorMessageElement.textContent = `${nameType} name cannot be empty.`;
        errorElement.hidden = false;
        errorMessageElement.hidden = false;
        backendErrorElement.hidden = true;
        if (nameType === "First") firstNameValid = false;
        else lastNameValid = false;
    } else if (!namePattern.test(input)) {
        errorMessageElement.textContent = `${nameType} name must only include letters, spaces, hyphens, or apostrophes.`;
        errorElement.hidden = false;
        errorMessageElement.hidden = false;
        backendErrorElement.hidden = true;
        if (nameType === "First") firstNameValid = false;
        else lastNameValid = false;
    } else if (input.length > 64) {
        errorMessageElement.textContent = `${nameType} name must be 64 characters long or less.`;
        errorElement.hidden = false;
        errorMessageElement.hidden = false;
        backendErrorElement.hidden = true;
        if (nameType === "First") firstNameValid = false;
        else lastNameValid = false;
    } else {
        errorMessageElement.textContent = "";
        errorElement.hidden = true;
        errorMessageElement.hidden = true;
        backendErrorElement.hidden = true;
        if (nameType === "First") firstNameValid = true;
        else lastNameValid = true;
    }
}

function checkPasswordStrength() {
    const password = passwordField.value;

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
        passwordFrontendErrorMessage.textContent = passwordErrors[0];
        passwordFrontendError.hidden = false;
        passwordFrontendErrorMessage.hidden = false;
        passwordBackendError.hidden = true;
        passwordValid = false;
    } else {
        passwordFrontendErrorMessage.textContent = "";
        passwordFrontendError.hidden = true;
        passwordFrontendErrorMessage.hidden = true;
        passwordBackendError.hidden = true;
        passwordValid = true;
    }
}

function checkPasswordMatch() {
    const password = passwordField.value;
    const confirmPassword = confirmPasswordField.value;

    if (password !== confirmPassword) {
        confirmPasswordFrontendErrorMessage.textContent = "Passwords do not match.";
        confirmPasswordFrontendError.hidden = false;
        confirmPasswordFrontendErrorMessage.hidden = false;
        confirmPasswordBackendError.hidden = true;
    } else {
        confirmPasswordFrontendErrorMessage.textContent = "";
        confirmPasswordFrontendError.hidden = true;
        confirmPasswordFrontendErrorMessage.hidden = true;
        confirmPasswordBackendError.hidden = true;
    }
}



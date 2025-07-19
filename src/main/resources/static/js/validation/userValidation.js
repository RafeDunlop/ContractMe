// Regex patterns
const emailPattern = /^[A-Za-z0-9]+([+_.-][A-Za-z0-9]+)*@[A-Za-z0-9]+([.-][A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
const namePattern = /^[\p{L}\-'\s]*$/u;

// Functions
export function checkEmailField(input, emailFrontendErrorMessage, emailFrontendError, emailBackendError) {
    input = input.trim();
    if (emailBackendError) emailBackendError.hidden = true;
    if (input === "" || !emailPattern.test(input)) {
        emailFrontendErrorMessage.textContent = "Email address must be in the form ‘jane@doe.nz’.";
        emailFrontendError.hidden = false;
        emailFrontendErrorMessage.hidden = false;
    } else {
        emailFrontendErrorMessage.textContent = "";
        emailFrontendError.hidden = true;
        emailFrontendErrorMessage.hidden = true;
    }
}

export function checkNameField(input, nameType, nameFrontendError, nameFrontendErrorMessage, nameBackendError) {
    input = input.trim();
    if (nameBackendError) nameBackendError.hidden = true;
    if (nameType === "First" && input === "") {
        nameFrontendErrorMessage.textContent = `${nameType} name cannot be empty.`;
        nameFrontendError.hidden = false;
        nameFrontendErrorMessage.hidden = false;
    } else if (!namePattern.test(input)) {
        nameFrontendErrorMessage.textContent = `${nameType} name must only include letters, spaces, hyphens, or apostrophes.`;
        nameFrontendError.hidden = false;
        nameFrontendErrorMessage.hidden = false;
    } else if (input.length > 64) {
        nameFrontendErrorMessage.textContent = `${nameType} name must be 64 characters long or less.`;
        nameFrontendError.hidden = false;
        nameFrontendErrorMessage.hidden = false;
    } else {
        nameFrontendErrorMessage.textContent = "";
        nameFrontendError.hidden = true;
        nameFrontendErrorMessage.hidden = true;
    }
}

export function checkPasswordStrength(passwordField, passwordFrontendError, passwordFrontendErrorMessage,
                               passwordBackendError) {
    const password = passwordField.value;

    const strengthError = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email).";

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

    if (passwordBackendError) passwordBackendError.hidden = true;

    if (passwordErrors.length > 0) {
        passwordFrontendErrorMessage.textContent = passwordErrors[0];
        passwordFrontendError.hidden = false;
        passwordFrontendErrorMessage.hidden = false;
    } else {
        passwordFrontendErrorMessage.textContent = "";
        passwordFrontendError.hidden = true;
        passwordFrontendErrorMessage.hidden = true;
    }
}

export function checkPasswordMatch(passwordField, confirmPasswordField, confirmPasswordFrontendError,
                            confirmPasswordFrontendErrorMessage, confirmPasswordBackendError) {
    const password = passwordField.value;
    const confirmPassword = confirmPasswordField.value;

    if (confirmPasswordBackendError) confirmPasswordBackendError.hidden = true;

    if (password !== confirmPassword) {
        confirmPasswordFrontendErrorMessage.textContent = "The new passwords do not match";
        confirmPasswordFrontendError.hidden = false;
        confirmPasswordFrontendErrorMessage.hidden = false;
    } else {
        confirmPasswordFrontendErrorMessage.textContent = "";
        confirmPasswordFrontendError.hidden = true;
        confirmPasswordFrontendErrorMessage.hidden = true;
    }
}

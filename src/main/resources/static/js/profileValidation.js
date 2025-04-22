// Elements
let emailField = document.getElementById("email");
let firstNameField = document.getElementById("first-name");
let lastNameField = document.getElementById("last-name");

let emailFrontendError = document.getElementById("email-frontend-error");
let emailFrontendErrorMessage = document.getElementById("email-frontend-error-message");
let emailBackendError = document.getElementById("email-backend-error");

let firstNameFrontendError = document.getElementById("first-name-frontend-error");
let lastNameFrontendError = document.getElementById("last-name-frontend-error");
let lastNameBackendError = document.getElementById("last-name-backend-error");

let firstNameFrontendErrorMessage = document.getElementById("first-name-frontend-error-message");
let lastNameFrontendErrorMessage = document.getElementById("last-name-frontend-error-message");
let firstNameBackendError = document.getElementById("first-name-backend-error");

// Regex patterns
const emailPattern = /^[A-Za-z0-9]+([+_.-][A-Za-z0-9]+)*@[A-Za-z0-9]+([.-][A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
const namePattern = /^[\p{L}\-'\s]{0,50}$/u;

// Validation functions
function checkEmailField(input) {
    input = input.trim();
    if (input === "" || !emailPattern.test(input)) {
        emailFrontendErrorMessage.textContent = "Email address must be in the form ‘jane@doe.nz’.";
        emailFrontendError.hidden = false;
    } else {
        emailFrontendError.hidden = true;
    }
    emailBackendError.hidden = true;
}

function checkNameField(input, type) {
    const errorDiv = type === "First" ? firstNameFrontendError : lastNameFrontendError;
    const errorMessage = type === "First" ? firstNameFrontendErrorMessage : lastNameFrontendErrorMessage;
    const backendErrorDiv = type === "First" ? firstNameBackendError : lastNameBackendError;

    input = input.trim();

    if (input === "" && type === "First") {
        errorMessage.textContent = `${type} name cannot be empty.`;
        errorDiv.hidden = false;
    } else if (!namePattern.test(input)) {
        errorMessage.textContent = `${type} name must only contain letters, hyphens, apostrophes, and spaces.`;
        errorDiv.hidden = false;
    } else {
        errorDiv.hidden = true;
    }
    backendErrorDiv.hidden = true;
}

// Event listeners
emailField.addEventListener("input", () => checkEmailField(emailField.value));
firstNameField.addEventListener("input", () => checkNameField(firstNameField.value, "First"));
lastNameField.addEventListener("input", () => checkNameField(lastNameField.value, "Last"));

const form = document.getElementById("edit-profile-form");

form.addEventListener("submit", function (e) {
    checkEmailField(emailField.value);
    checkNameField(firstNameField.value, "First");
    checkNameField(lastNameField.value, "Last");

});

const validImageTypes = ['image/jpeg', 'image/png', 'image/jpg']; // Allowed image types
const maxFileSize = 5 * 1024 * 1024; // 5 MB in bytes

function validateProfilePicture(file) {
    let fileValidationError = document.getElementById('profile-picture-frontend-error');
    let fileValidationErrorList = document.getElementById('profile-picture-error-list');
    fileValidationErrorList.innerHTML = "";

    if (file === null || file === undefined) {
        return;
    }

    let errors = [];

    if (!validImageTypes.includes(file.type)) {
        errors.push("Only JPEG, PNG, and JPG files are allowed.");
    }

    if (file.size > maxFileSize) {
        errors.push("File size must be less than 5 MB.");
        document.querySelector('input[type="submit"]').disabled = true; // Prevent issues with uploading large files
    }

    if (errors.length > 0) {
        errors.forEach(error => {
            let listItem = document.createElement('li');
            listItem.textContent = error;
            fileValidationErrorList.appendChild(listItem);
        });
        fileValidationError.hidden = false;
        return false;
    }

    fileValidationError.hidden = true;
    return true;
}

function updateFileName() {
    let fileInput = document.getElementById('formFile');
    let fileLabel = document.getElementById('fileLabel');
    let file = fileInput.files[0];

    let maxLength = 30;
    let fileValid = validateProfilePicture(file);

    if (fileValid) {
        if (file) {
            let fileName = file.name;
            // If filename is too long, reset to "File selected"
            fileLabel.textContent = fileName.length > maxLength ? "File selected" : fileName;
        } else {
            fileLabel.textContent = "Edit Profile Picture";
        }
    } else {
        fileLabel.textContent = "Invalid file"; // Set label to indicate invalid file
    }
}

document.getElementById('formFile').addEventListener('change', updateFileName);


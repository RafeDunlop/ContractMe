import { validateField } from "./renovationCommons.js";

let recordNameField = document.getElementById("name")
let nameFrontendError = document.getElementById("name-frontend-error")
let nameFrontendErrorMessage = document.getElementById("name-frontend-error-message")
let nameBackendError = document.getElementById("name-backend-error")
let recordDescriptionField = document.getElementById("description")
let descriptionFrontendError = document.getElementById("description-frontend-error")
let descriptionFrontendErrorMessage = document.getElementById("description-frontend-error-message")
let descriptionBackendError = document.getElementById("description-backend-error-message")


let nameFieldValid = false;
let descriptionFieldValid = true;

// Error message if the room name is invalid
const roomNameErrorMessage = "Renovation record name must only include letters, numbers, spaces, dots, " +
    "hyphens or apostrophes"

recordNameField.addEventListener("input", function () {checkNameField(recordNameField.value)})

recordDescriptionField.addEventListener("input", function () {checkDescriptionField(recordDescriptionField.value)})

document.addEventListener("DOMContentLoaded", function () {


    const form = document.getElementById("renovation-form");
    if (form) {
        form.addEventListener("submit", function () {
            const roomNames = collectRoomNames();
            const roomListEditField = document.getElementById("roomListEdit");
            if (roomListEditField) {
                roomListEditField.value = roomNames.join(",");
            }
        });
    }
});

/**
 * Checks the name fields input validity, called by event listener
 * If input is currently null displays appropriate error message.
 * Calls toggle submit button which checks whether the form can be submitted
 * @param input from the form
 */
function checkNameField(input) {
    input = input.trim();

    const isEditMode = document.title === "Edit renovation";
    const existingName = document.getElementById("existingName")?.value;

    if (input === "") {
        nameFrontendErrorMessage.textContent = "Renovation record name cannot be empty.";
        nameFrontendError.hidden = false;
        nameFrontendErrorMessage.hidden = false;
        nameBackendError.hidden = true;
        nameFieldValid = false;

    } else if (!isEditMode && existingName && input.toLowerCase() === existingName.toLowerCase()) {
        // In CREATE mode, block if name matches existing
        nameFrontendErrorMessage.textContent = "You already have a renovation with this name.";
        nameFrontendError.hidden = false;
        nameFrontendErrorMessage.hidden = false;
        nameBackendError.hidden = true;
        nameFieldValid = false;

    } else if (isEditMode && existingName && input.toLowerCase() === existingName.toLowerCase()) {
        // In EDIT mode and name hasn't changed, it's valid
        nameFieldValid = true;
        nameFrontendError.hidden = true;
        nameFrontendErrorMessage.textContent = "";
        nameBackendError.hidden = true;

    } else if (input.length > 128) {
        // In CREATE mode and name has less than 128 characters, it's valid
        nameFrontendErrorMessage.textContent = " Name cannot be greater than 128 characters.";
        nameFrontendError.hidden = false;
        nameFrontendErrorMessage.hidden = false;
        nameBackendError.hidden = true;
        nameFieldValid = false;

    } else {
        const isValid = validateField(input, /^[\p{L}\d .,\-']*$/u, nameFrontendErrorMessage, roomNameErrorMessage);
        nameFieldValid = isValid;
        nameFrontendError.hidden = isValid;
        nameBackendError.hidden = true;

        if (isValid) {
            nameFrontendErrorMessage.textContent = "";
        }
    }
}

/**
 * Checks the validity of the description form field, called by event listener
 * If the length of the input is above 512 displays error message
 * @param input from the form to be checked
 */
function checkDescriptionField(input) {
    if (input.length > 512) {
        descriptionFrontendErrorMessage.textContent = "Renovation record description must be 512 characters or less";
        descriptionFrontendError.hidden = false;
        descriptionFrontendErrorMessage.hidden = false;
        descriptionBackendError.hidden = true;
        descriptionFieldValid = false;
    } else {
        descriptionFrontendErrorMessage.textContent = "";
        descriptionFrontendError.hidden = true;
        descriptionFrontendErrorMessage.hidden = true;
        descriptionBackendError.hidden = true;
        descriptionFieldValid = true;
    }
}

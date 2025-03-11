import { validateField } from "./validations.js";

let recordNameField = document.getElementById("name")
let nameErrorLabel = document.getElementById("name-error-message")
let recordDescriptionField = document.getElementById("description")
let descriptionErrorLabel = document.getElementById("description-error-message")
let submitButton = document.getElementById("submit-record")

let nameFieldValid = false;
let descriptionFieldValid = true;

// Error message if the room name is invalid
const roomNameErrorMessage = "Renovation record name must only include letters, numbers, spaces, dots, " +
    "hyphens or apostrophes"

recordNameField.addEventListener("input", function () {checkNameField(recordNameField.value)})

recordDescriptionField.addEventListener("input", function () {checkDescriptionField(recordDescriptionField.value)})

document.addEventListener("DOMContentLoaded", function () {
    if (document.title === "Edit renovation") {
        checkNameField(recordNameField.value);
    }
    checkDescriptionField(recordDescriptionField.value);
    if (recordNameField.value !== "") checkNameField(recordNameField.value);
});

/**
 * Checks the name fields input validity, called by event listener
 * If input is currently null displays appropriate error message.
 * Calls toggle submit button which checks whether the form can be submitted
 * @param input from the form
 */
function checkNameField(input) {
    if (input.trim() === "") {
        nameErrorLabel.textContent = "Renovation record name cannot be empty";
        nameErrorLabel.hidden = false;
        nameFieldValid = false;
    } else if (input.trim() === document.getElementById("existingName").value) {
        nameFieldValid = false;
        nameErrorLabel.hidden = false;
        nameErrorLabel.textContent = "You already have a renovation with this name";
    } else {
        nameErrorLabel.textContent = roomNameErrorMessage;
        nameFieldValid = validateField(input, /^[\p{L}\d .,\-']*$/u, nameErrorLabel, roomNameErrorMessage);
    }
    if (nameFieldValid) {
        nameErrorLabel.textContent = "";
    }
    toggleSubmitButton();
}

/**
 * Checks the validity of the description form field, called by event listener
 * If the length of the input is above 512 displays error message
 * @param input from the form to be checked
 */
function checkDescriptionField(input) {
    if (input.length > 512) {
        descriptionErrorLabel.textContent = "Renovation record description must be 512 characters or less";
        descriptionFieldValid = false;
        descriptionErrorLabel.hidden = false;
    } else {
        descriptionFieldValid = true;
        descriptionErrorLabel.textContent = "";
        descriptionErrorLabel.hidden = true;
    }
    toggleSubmitButton()
}

/**
 * Function that toggles the form submission button based on whether both input fields have a valid input
 */
function toggleSubmitButton() {
    submitButton.disabled = nameFieldValid === false || descriptionFieldValid === false;
}
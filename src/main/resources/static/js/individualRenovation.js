import {
    updateCharCounter,
    injectRoomsIntoSubmission,
    validateField,
    renderRooms
} from "./renovationCommons.js";

let roomFieldValid = false;
let roomList = [];


const roomNameErrorMessage =
    "Renovation record room names must only include letters, numbers, spaces, " +
    "dots, hyphens or apostrophes";

let roomFrontendError, roomFrontendErrorMessage, roomBackendError;
let roomNameField, createRoomButton, descriptionTextField, form;

/**
 * Runs when the document loads.
 * Adds event listeners on html elements and extracts the roomList
 */
document.addEventListener("DOMContentLoaded", () => {
    roomFrontendError = document.getElementById("room-frontend-error");
    roomFrontendErrorMessage = document.getElementById("room-frontend-error-message");
    roomBackendError = document.getElementById("room-backend-error");
    roomNameField = document.getElementById("roomList");
    createRoomButton = document.getElementById("create-room-button");
    descriptionTextField = document.getElementById("description");
    form = document.getElementById("renovation-form");

    // Now everything below can use those elements
    form.addEventListener("submit", function (event) {
        injectRoomsIntoSubmission(form, event, roomList)
    });

    roomNameField.addEventListener("input", function () {
        checkRoomName(roomNameField.value);
        let roomError = getRoomNameError(roomNameField.value.trim());
        const isError = roomError !== null;
        roomFrontendError.hidden = !isError;
        roomFrontendErrorMessage.hidden = !isError;
        if (isError) {
            roomFrontendErrorMessage.textContent = roomError;
        }
    });

    createRoomButton.addEventListener("click", addRoom);


    descriptionTextField.addEventListener("input", function () {
        updateCharCounter("description", "description-length-counter")
    });

    let hiddenRoomInputs = document.querySelectorAll('#room-hidden-inputs input[type="hidden"]');
    let previousRoomList = Array.from(hiddenRoomInputs).map(input => input.value);
    setRoomList(previousRoomList, "room-list");
    updateCharCounter("description", "description-length-counter");
});

document.getElementById("roomList").addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
        event.preventDefault();
        document.getElementById("create-room-button").click();
    }
});

function checkRoomName(input) {
    roomFieldValid = validateField(input, /^[\p{L}\d .,\-']*$/u, roomFrontendErrorMessage, roomNameErrorMessage);
}

function addRoom() {
    let room = roomNameField.value.trim();
    if (room === "") {
        console.log("No room entered.");
        return;
    }

    let roomError = getRoomNameError(room);
    const isError = roomError !== null;
    roomFrontendErrorMessage.hidden = !isError;

    if (!isError) {
        roomNameField.value = ""; // Reset input value
        roomFrontendError.hidden = true;
        roomFrontendErrorMessage.hidden = true;
        roomBackendError.hidden = true;

        // Add room to the list
        roomList.push(room);

        // Render rooms in the frontend (updates the UI with the new room)
        renderRooms(roomList, "room-list");

        // Create and append hidden input to the room-hidden-inputs div
        let input = document.createElement("input");
        input.type = "hidden";
        input.name = "roomList";
        input.value = room;
        document.getElementById("room-hidden-inputs").appendChild(input);

    } else {
        roomFrontendErrorMessage.textContent = roomError;
        roomFrontendErrorMessage.hidden = false;
        roomFrontendError.hidden = false;
        roomBackendError.hidden = true;
    }
}

function setRoomList(previousRoomList, roomTableId) {
    if (previousRoomList !== null) {
        roomList = previousRoomList;
        renderRooms(roomList, roomTableId);

        // Also inject hidden inputs for each room from the model
        const hiddenInputsDiv = document.getElementById("room-hidden-inputs");
        if (hiddenInputsDiv) {
            hiddenInputsDiv.innerHTML = "";
        }
    }
}

function getRoomNameError(toAdd) {
    let error = null;
    const validCharactersPattern = /^[\p{L}\d .,\-']*$/u;
    if (!validCharactersPattern.test(toAdd)) {
        error = "Renovation record room names must only contain letters, numbers, spaces, dots, hyphens or apostrophes";
    } else if(toAdd.length >= 100) {
        error = "Renovation record room names must be less than 100 characters";
    }
    else if (roomList.indexOf(toAdd, 0) !== -1) {
        error = "You already have a room with this name"
    }
    return error;
}

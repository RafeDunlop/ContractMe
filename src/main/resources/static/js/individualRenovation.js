import {
    updateCharCounter,
    injectRoomsIntoSubmission,
    validateField,
    renderRooms
} from "./renovationCommons.js";

let roomFieldValid = false;
let roomList = [];
let roomErrorLabel = document.getElementById("room-error-message")
let roomNameField = document.getElementById("roomList")
let createRoomButton = document.getElementById("create-room-button")
let descriptionTextField = document.getElementById("description")
let form = document.getElementById("renovation-form");

const roomNameErrorMessage =
    "Renovation record room names must only include letters, numbers, spaces, " +
    "dots, hyphens or apostrophes"

/**
 * Runs when the document loads.
 * Adds event listeners on html elements and extracts the roomList
 */
document.addEventListener("DOMContentLoaded", () => {
    form.addEventListener("submit", function (event) { injectRoomsIntoSubmission(form, event, roomList) });
    roomNameField.addEventListener("input", function() { checkRoomName(roomNameField.value) });
    createRoomButton.addEventListener("click", function() { addRoom('roomList', 'room-list'     ) });
    descriptionTextField.addEventListener("input", function() {
        updateCharCounter("description", "description-length-counter")
    });
    let previousRoomList, previousRoomListString;
    previousRoomListString = document.getElementById('roomListEdit').value;
    if (previousRoomListString === "") {
        previousRoomList = [];
    } else {
        previousRoomList = previousRoomListString.split(',');
    }
    console.log(previousRoomList.type);
    setRoomList(previousRoomList, "room-list");
    updateCharCounter("description", "description-length-counter");
});

function checkRoomName(input) {
    roomFieldValid = validateField(input, /^[\p{L}\d .,\-']*$/u, roomErrorLabel, roomNameErrorMessage);
}

function addRoom(inputId, roomTableId) {
    let input = document.getElementById(inputId);
    let room = input.value.trim();
    if (room === "") {
        // If the room is empty, don't add it to the list, just return.
        console.log("No room entered.");
        return;
    }
    let roomError = getRoomNameError(room)
    const isError = roomError !== null
    roomErrorLabel.hidden = ! isError
    if (!isError) {
        input.value = "";
        roomList.push(room);
        renderRooms(roomList, roomTableId);
    } else {
        roomErrorLabel.textContent = roomError
    }
}

function setRoomList(previousRoomList, roomTableId) {
    if (previousRoomList !== null) {
        roomList = previousRoomList;
        renderRooms(roomList, roomTableId);
    }
}

function getRoomNameError(toAdd) {
    let error = null;
    const validCharactersPattern = /^[\p{L}\d .,\-']*$/u;
    if (!validCharactersPattern.test(toAdd)) {
        error = "Renovation record room names must only contain letters, numbers, spaces, dots, hyphens or apostrophes";
    } else if(toAdd.length >= 255) {
        error = "Renovation record room names must be less than 255 characters";
    }
    else if (roomList.indexOf(toAdd, 0) !== -1) {
        error = "You already have a room with this name"
    }
    return error;
}

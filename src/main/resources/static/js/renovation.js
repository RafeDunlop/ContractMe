import {setRoomList, addRoom, updateCharCounter, checkRoomName, addRoomsToSubmission} from "./validations.js";




let roomErrorLabel = document.getElementById("room-error-message")
let roomNameField = document.getElementById("roomList")
let createRoomButton = document.getElementById("create-room-button")
let descriptionTextField = document.getElementById("description")


addRoomsToSubmission("renovation-form")


const roomNameErrorMessage = "Renovation record room names must only include letters, numbers, spaces, " +
    "dots, hyphens or apostrophes"
roomNameField.addEventListener("input", function() {checkRoomName(roomNameField.value)});

createRoomButton.addEventListener("click", function() {addRoom('roomList')});
descriptionTextField.addEventListener("input", function() {
    updateCharCounter("description", "description-length-counter")
});

document.addEventListener("DOMContentLoaded", () => {
    let previousRoomList, previousRoomListString;
    previousRoomListString = document.getElementById('roomListEdit').value;
    if (previousRoomListString === "") {
        previousRoomList = [];
    } else {
        previousRoomList = previousRoomListString.split(',');
    }
    console.log(previousRoomList.type);
    setRoomList(previousRoomList);
    updateCharCounter("description", "description-length-counter");
});

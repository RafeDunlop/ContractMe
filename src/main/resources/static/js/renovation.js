import { validateField } from "./validations.js";

let roomErrorLabel = document.getElementById("room-error-message")
let roomNameField = document.getElementById("roomList")
let createRoomButton = document.getElementById("create-room-button")
let descriptionTextField = document.getElementById("description")

let roomList = [];
let roomId = 0;
let roomFieldValid = false;
const form = document.getElementById("renovation-form");

form.addEventListener("submit", function(event) {
    let input;
    event.preventDefault();
    form.querySelectorAll("input[name='roomList']").forEach(room => room.remove());
    roomList.forEach(room => {
        input = document.createElement("input");
        input.type = "hidden";
        input.name = "roomList";
        input.value = room[1];
        form.appendChild(input);
    });
    form.submit();
});

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
    setRoomList(previousRoomList);
    updateCharCounter("description", "description-length-counter");
});

function setRoomList(previousRoomList) {
    if (previousRoomList !== null) {
        previousRoomList.forEach(room => {
            roomList.push([roomId++, room]);
        });
        renderRooms();
    }
}

function updateCharCounter(textAreaId, counterId, maxLimit = 512) {
    let textArea = document.getElementById(textAreaId);
    let counter = document.getElementById(counterId);
    let length = textArea.value.length;
    counter.textContent = `${length}/${maxLimit}`;
}

function addRoom(inputId) {
    let input = document.getElementById(inputId);
    let room = input.value.trim();
    if (room === "") {
        // If the room is empty, don't add it to the list, just return.
        return;
    }
    let roomError = getRoomError(room)
    if (roomError == null) {
        input.value = "";
        roomList.push([roomId++, room]);
        renderRooms();
    } else {
        //error text becomes room error
    }
}

function getRoomError(toAdd) {
    let error = null;
    const validCharactersPattern = /^[\p{L}\d .,\-']*$/u;
    if (!validCharactersPattern.test(toAdd)) {
        error = "Renovation record room names must only contain letters, numbers, spaces, dots, hyphens or apostrophes";
    }
    return error;
}

function renderRooms() {
    let i, room, roomTable, tableRow, roomCell, buttonCell, button;
    roomTable = document.getElementById("room-list");
    roomTable.innerHTML = "";
    for (i=0; i<roomList.length; i++) {
        room = roomList[i];
        tableRow = roomTable.insertRow();
        roomCell = tableRow.insertCell(0);
        roomCell.innerText = room[1];
        buttonCell = tableRow.insertCell(1);
        buttonCell.style.textAlign = "right";
        buttonCell.style.width = "50px";
        button = document.createElement("button");
        button.className = "btn btn-primary";
        button.innerHTML = "❌";
        button.style.backgroundColor = "white";
        button.style.border = "1px solid #ccc";
        button.style.cursor = "pointer";
        button.background = "white";
        button.onclick = function () {
            roomList = roomList.filter(thisRoom => thisRoom[0] !== room[0]);
            renderRooms();
        };
        buttonCell.appendChild(button);
    }
}

function checkRoomName(input) {
    roomFieldValid = validateField(input, /^[\p{L}\d .,\-']*$/u, roomErrorLabel, roomNameErrorMessage);
}

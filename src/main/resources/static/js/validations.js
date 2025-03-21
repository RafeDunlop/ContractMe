let roomId = 0;
let roomFieldValid = false;
let roomList = [];

/**
 * Validates a given input field based off the provided regular expression pattern
 * If the input is valid the error label is hidden, if it isn't the appropriate error message is displayed
 * @param input in the form field currently
 * @param pattern the regular expression to test the input against
 * @param errorLabel the error label to display any errors
 * @param errorMessage the message to be displayed in the label
 * @returns {boolean} true if the input is valid, false otherwise
 */
export function validateField(input, pattern, errorLabel, errorMessage) {
    let isValidInput = pattern.test(input)
    if (isValidInput) {
        errorLabel.hidden = true;
        return true;
    } else {
        errorLabel.hidden = false;
        errorLabel.textContent = errorMessage;
        return false;
    }
}

export function addRoomsToSubmission(formId) {
    const form = document.getElementById(formId);
    form.addEventListener("submit", function (event) {
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
}

export function renderRooms(roomList) {
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
            renderRooms(roomList);
        };
        buttonCell.appendChild(button);
    }
}

export function setRoomList(previousRoomList) {
    roomId = 0;
    roomList = []
    if (previousRoomList !== null) {
        previousRoomList.forEach(room => {
            roomList.push([roomId++, room]);
        });
        renderRooms(roomList);
    }
}


export function updateCharCounter(textAreaId, counterId, maxLimit = 512) {
    let textArea = document.getElementById(textAreaId);
    let counter = document.getElementById(counterId);
    let length = textArea.value.length;
    counter.textContent = `${length}/${maxLimit}`;
}

export function addRoom(inputId) {
    let input = document.getElementById(inputId);
    let room = input.value.trim();
    if (room === "") {
        // If the room is empty, don't add it to the list, just return.
        console.log("No room entered.");
        return;
    }
    let roomError = getRoomError(room)
    if (roomError == null) {
        input.value = "";
        roomList.push([roomId++, room]);
        renderRooms(roomList);
    } else {
        //error text becomes room error
    }
}

export function getRoomError(toAdd) {
    let error = null;
    const validCharactersPattern = /^[\p{L}\d .,\-']*$/u;
    if (!validCharactersPattern.test(toAdd)) {
        error = "Renovation record room names must only contain letters, numbers, spaces, dots, hyphens or apostrophes";
    }
    return error;
}

export function checkRoomName(input) {
    roomFieldValid = validateField(input, /^[\p{L}\d .,\-']*$/u, roomErrorLabel, roomNameErrorMessage);
}

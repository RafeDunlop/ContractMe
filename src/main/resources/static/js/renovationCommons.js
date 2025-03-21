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

export function addRoomsToSubmission(formId, roomList) {
    const form = document.getElementById(formId);
    form.addEventListener("submit", function (event) {
        let input;
        event.preventDefault();
        form.querySelectorAll("input[name='roomList']").forEach(room => room.remove());
        roomList.forEach(room => {
            input = document.createElement("input");
            input.type = "hidden";
            input.name = "roomList";
            input.value = room;
            form.appendChild(input);
        });
        form.submit();
    });
}

export function updateCharCounter(textAreaId, counterId, maxLimit = 512) {
    let textArea = document.getElementById(textAreaId);
    let counter = document.getElementById(counterId);
    let length = textArea.value.length;
    counter.textContent = `${length}/${maxLimit}`;
}

export function renderRooms(roomList, roomTableId) {
    let i, room, roomTable, tableRow, roomCell, buttonCell, button;
    roomTable = document.getElementById(roomTableId);
    roomTable.innerHTML = "";
    for (i=0; i<roomList.length; i++) {
        const finalI = i;
        room = roomList[i];
        tableRow = roomTable.insertRow();
        roomCell = tableRow.insertCell(0);
        roomCell.innerText = room;
        buttonCell = tableRow.insertCell(1);
        buttonCell.style.textAlign = "right";
        buttonCell.style.width = "50px";
        button = document.createElement("button");
        button.className = "btn btn-primary";
        button.innerText = "❌";
        button.style.backgroundColor = "white";
        button.style.border = "1px solid #ccc";
        button.style.cursor = "pointer";
        button.background = "white";
        button.onclick = () => {
            roomList.splice(finalI, 1);
            renderRooms(roomList, roomTableId);
        }
        buttonCell.appendChild(button);
    }
}
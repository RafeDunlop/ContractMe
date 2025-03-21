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

/**
 * intercepts a form upon submission and injects the specified roomList into the submission
 * @param form The form being submitted
 * @param event The submission event
 * @param roomList The array whose entries will be added to the form's submission at the time of submission
 */
export function injectRoomsIntoSubmission(form, event, roomList) {
    let input;
    event.preventDefault();
    form.querySelectorAll("input[name='roomList']").forEach(room => room.remove());
    console.log("room list upon form submission:");
    console.log(roomList);
    roomList.forEach(room => {
        input = document.createElement("input");
        input.type = "hidden";
        input.name = "roomList";
        input.value = room;
        form.appendChild(input);
    });
    form.submit();
}

/**
 * updates the specified counter to display the length of the specified textArea relative to a specified or default maximum
 * format is <strong>textArea.length/maxLimit</strong>
 * maximum is based on storage space, so Unicode symbols encoded with 2 characters count as 2 characters
 * @param textAreaId The id of the TextArea whose length is checked
 * @param counterId The id of the counter whose text content is set
 * @param maxLimit The maximum to display. Default is 512
 */
export function updateCharCounter(textAreaId, counterId, maxLimit = 512) {
    let textArea = document.getElementById(textAreaId);
    let counter = document.getElementById(counterId);
    let length = textArea.value.length;
    counter.textContent = `${length}/${maxLimit}`;
}

/**
 * Clears the specified table and fills it with the specified roomList
 * Each room has a "delete" button which removes the associated room from the specified list
 * @param roomList The list to be rendered
 * @param roomTableId The id of the table to render the list into
 */
export function renderRooms(roomList, roomTableId) {
    console.log("renderRooms");
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
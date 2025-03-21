import {
    updateCharCounter,
    injectRoomsIntoSubmission,
    renderRooms
} from "./renovationCommons.js";

let roomList;
let selectedList = []
let roomSelection = document.getElementById("roomSelection");
let form = document.getElementById("create-task-form");

/**
 * Event listener that runs when the create task page is loaded.
 * Initialises the live page elements.
 */
document.addEventListener("DOMContentLoaded", () => {
    let previousRoomList, previousRoomListString;
    previousRoomListString = document.getElementById('availableRoomList').value;
    if (previousRoomListString === "") {
        previousRoomList = [];
    } else {
        previousRoomList = previousRoomListString.split(',');
    }
    roomList = previousRoomList
    updateCharCounter("description", "description-length-counter");
    populateRoomSelection();
    form.addEventListener("submit", function (event) { injectRoomsIntoSubmission(form, event, roomList) })
    document.getElementById("roomSelection").onclick = () => populateRoomSelection();
    roomSelection.addEventListener("change", function () { selectRoom(this.value) });
});

/**
 * Renders a display element for a room that's been selected.
 * @param room
 */
function selectRoom(room) {
    selectedList.push(room);
    renderRooms(selectedList, "room-table");
}

/**
 * Populates the drop-down box with the rooms available for selection.
 * Is run every time an option is clicked.
 */

function populateRoomSelection() {
    roomSelection.length = 0;
    let unselected = unselectedList();
    if (unselected.length > 0) {
        for (let i = 0; i < roomList.length; i++) {
            let option = document.createElement("option");
            option.value = unselected[i];
            option.textContent = unselected[i];
            roomSelection.appendChild(option);
        }
    }
    else {
        const emptyListOption = document.createElement("option");
        emptyListOption.value = "";
        emptyListOption.textContent = "There are no available rooms to select";
        emptyListOption.disabled = true;
        emptyListOption.selected = true;
        roomSelection.appendChild(emptyListOption);
    }
}

/**
 * Populates the list that displays the options in the drop-down box
 * @returns {*[]}
 */
function unselectedList() {
    let i, withoutSelected, selectedRoom;
    withoutSelected = [];
    roomList.forEach(room => withoutSelected.push(room));
    for (i = 0; i < selectedList.length; i++) {
        selectedRoom = selectedList[i];
        withoutSelected = withoutSelected.filter(thisRoom => thisRoom !== selectedRoom);
    }
    return withoutSelected;

}



import {
    updateCharCounter,
    addRoomsToSubmission,
    renderRooms
} from "./renovationCommons.js";

let roomList;
let selectedList = []
let roomSelection = document.getElementById("roomSelection");

document.addEventListener("DOMContentLoaded", () => {
    let previousRoomList, previousRoomListString;
    previousRoomListString = document.getElementById('availableRoomList').value;
    if (previousRoomListString === "") {
        previousRoomList = [];
    } else {
        previousRoomList = previousRoomListString.split(',');
    }
    console.log(previousRoomList.type);
    roomList = previousRoomList
    updateCharCounter("description", "description-length-counter");
    populateRoomSelection();
    addRoomsToSubmission("create-task-form", selectedList);
    document.getElementById("roomSelection").onclick = () => populateRoomSelection();
    roomSelection.addEventListener("change", function () { selectRoom(this.value) });
});

function selectRoom(room) {
    selectedList.push(room);
    renderRooms(selectedList, "room-table");
}

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
    } else {

    }
}

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



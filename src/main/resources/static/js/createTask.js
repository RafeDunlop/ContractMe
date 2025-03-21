import {
    setRoomList,
    addRoom,
    updateCharCounter,
    checkRoomName,
    addRoomsToSubmission,
    renderRooms
} from "./validations.js";

let roomList;
let selectedList = []
let roomId = 0;
let roomSelection = document.getElementById("roomSelection");
const form = document.getElementById("create-task-form");

document.addEventListener("DOMContentLoaded", () => {
    let previousRoomList, previousRoomListString;
    previousRoomListString = document.getElementById('availableRoomList').value;
    if (previousRoomListString === "") {
        previousRoomList = [];
    } else {
        previousRoomList = previousRoomListString.split(',');
    }
    console.log(previousRoomList.type);
    setRoomList(previousRoomList);
    roomList = previousRoomList
    updateCharCounter("description", "description-length-counter");
    populateRoomSelection();
    document.getElementById("roomSelection").onclick = () => populateRoomSelection();
});

function selectRoom(room) {
    selectedList.push(room);
    renderRooms(selectedList);
}

function populateRoomSelection() {
    console.log("all: " + roomList);
    console.log("selected: " + selectedList);
    let i, length;
    length = roomSelection.options.length - 1
    for (i = length; i >= 0; i--) {
        roomSelection.options.remove(i);
    }
    let unselected = unselectedList();
    console.log("unselected: " + unselected);
    if (unselected.length > 0) {
    for (let i = 0; i < roomList.length; i++) {
        let option = document.createElement("option");
        option.value = unselected[i];
        option.textContent = unselected[i];
        option.onclick = () => selectRoom(unselected[i]);
        roomSelection.appendChild(option);
    }

}   else {


}}

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

function renderSelectedRooms() {

}



import {setRoomList, addRoom, updateCharCounter, checkRoomName, addRoomsToSubmission} from "./validations.js";

let roomList;
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
});

function populateRoomSelection() {

    if (roomList.length > 0) {
    for (let i = 0; i < roomList.length; i++) {
        let option = document.createElement("option");
        option.value = roomList[i];
        option.textContent = roomList[i];
        roomSelection.appendChild(option);

    }

}   else {


}}



import {setRoomList, addRoom, updateCharCounter, checkRoomName, addRoomsToSubmission} from "./validations.js";

let roomList = document.getElementById("roomList").value;
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
    updateCharCounter("description", "description-length-counter");
});

function populateRoomSelection() {
    console.log("b");
    console.log(roomList);
    console.log(roomList[0]);
    console.log(typeof roomList);
    console.log(roomList.length)


    roomSelection.innerHTML = "";

    if (roomList.length > 0) {
    for (let i = 0; i < roomList.length; i++) {
        let option = document.createElement("option");
        option.value = roomList[i];
        option.textContent = roomList[i];
        console.log(option.textContent);
        roomSelection.appendChild(option);
        console.log("d")
    }

}   else {

}}



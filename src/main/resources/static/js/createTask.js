

let roomList = document.getElementById("roomList");
let roomId = 0;
let roomFieldValid = false;
let roomSelection = document.getElementById("roomSelection");
const form = document.getElementById("create-task-form");



function setRoomList(previousRoomList) {
    console.log(previousRoomList);
    if (previousRoomList !== null) {
        previousRoomList.forEach(room => {
            roomList.push([roomId++, room]);
        });
        renderRooms();
    }
}

document.addEventListener("DOMContentLoaded", () => {
    let previousRoomList, previousRoomListString;

    console.log(roomList);

    //updateCharCounter("description", "description-length-counter");
});


function populateRoomSelection() {

    roomSelection.innerHTML = "";

    if (roomList.length > 0) {
    for (let i = 0; i < roomList.length; i++) {
        let option = document.createElement("option");
        option.value = roomList[i];
        option.textContent = roomList[i];
        roomSelection.appendChild(option);
    }

}   else {

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





}



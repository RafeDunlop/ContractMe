

let roomList = document.getElementById("roomList").value;
let roomId = 0;
let roomSelection = document.getElementById("roomSelection");
const form = document.getElementById("create-task-form");




document.addEventListener("DOMContentLoaded", () => {
    let input;
    //event.preventDefault();
    console.log(roomList);
    form.querySelectorAll("input[name='roomList']").forEach(room => room.remove());
        console.log(roomList);
        roomList.forEach(room => {
        console.log("a4");
            input = document.createElement("input");
            input.type = "hidden";
            input.name = "roomList";
            input.value = room[1];
            form.appendChild(input);
    populateRoomSelection();
    renderRooms();
});

});
    //updateCharCounter("description", "description-length-counter");



function populateRoomSelection() {
    console.log("b");
    console.log(roomList);
    console.log(roomList[0]);
    console.log(typeof roomList);
    console.log(roomList.length)

    if (roomList === null) {
        console.log("myList is null");
    } else if (roomList === undefined) {
        console.log("myList is undefined");
    } else if (!Array.isArray(roomList)) {
        console.log("myList is not an array");
        console.log(Object.prototype.toString.call(roomList));
    } else if (roomList.length === 0) {
        console.log("myList is an empty array");
    } else {
        console.log("myList is populated:", roomList);
    }

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




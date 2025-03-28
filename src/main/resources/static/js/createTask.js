import {
    updateCharCounter,
    injectRoomsIntoSubmission,
    renderRooms
} from "./renovationCommons.js";

let roomList;
let selectedList = []
let form = document.getElementById("create-task-form");

/**
 * Event listener that runs when the create task page is loaded.
 * Initialises the live page elements.
 */
document.addEventListener("DOMContentLoaded", () => {
    updateCharCounter("description", "description-length-counter");
    document.getElementById("description").
    addEventListener("input", () => updateCharCounter("description", "description-length-counter"))
});


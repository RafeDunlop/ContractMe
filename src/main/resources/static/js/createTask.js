import {
    updateCharCounter
} from "./renovationCommons.js";

/**
 * Event listener that runs when the create task page is loaded.
 * Initialises the live page elements.
 */
document.addEventListener("DOMContentLoaded", () => {
    updateCharCounter("description", "description-length-counter");
    document.getElementById("description").
    addEventListener("input", () => updateCharCounter("description", "description-length-counter"))
});


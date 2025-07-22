import {hideAllErrorMessages} from "./validation/locationFormValidation.js";

let locationToggleSwitch = document.getElementById("location-toggleswitch");
let locationForm = document.getElementById("location-form");

locationToggleSwitch.addEventListener("click", displayLocationForm);

/**
 * Display location form when location toggle switch is activated and hide form when deactivated.
 * Locks location toggle button if contractor toggle switch is active.
 */
function displayLocationForm() {
    if (locationToggleSwitch.checked === true) {
        locationForm.style.display = "block";
    } else {
        locationForm.style.display = "none";
        hideAllErrorMessages();
    }
}

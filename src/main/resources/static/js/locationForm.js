import {
    hideAllErrorMessages
} from "./locationFormValidation.js";

let locationToggleSwitch = document.getElementById("location-toggleswitch");
let locationForm = document.getElementById("location-form");


locationToggleSwitch.addEventListener("click", displayLocationForm);

/**
 * Displays location form when toggle switch is clicked
 */
function displayLocationForm() {
    if (locationToggleSwitch.checked === true) {
        locationForm.style.display = "block";

    }
    else {
        locationForm.style.display = "none";
        hideAllErrorMessages()

    }
}




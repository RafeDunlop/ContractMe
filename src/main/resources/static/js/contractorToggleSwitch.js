import {hideAllErrorMessages} from "./validation/locationFormValidation.js";

let locationToggleSwitch = document.getElementById("location-toggleswitch");
let locationForm = document.getElementById("location-form");
let locationToggleSwitchLabel = document.getElementById("location-toggle-switch-label");

let contractorToggleSwitch = document.getElementById("contractor-toggleswitch");
let contractorForm = document.getElementById("contractor-form");

contractorToggleSwitch.addEventListener("click", displayContractorForm);


/**
 * Display contractor form and location form when contractor toggle switch is activated and hide both forms when deactivated.
 */
function displayContractorForm() {
    if (contractorToggleSwitch.checked === true) {
        locationToggleSwitch.checked = true;
        locationForm.style.display = "block";
        contractorForm.style.display = "block";
        locationToggleSwitchLabel.innerText = "Enter Location (Required):"
    }
    else {
        locationToggleSwitch.checked = false;
        locationForm.style.display = "none";
        contractorForm.style.display = "none";
        hideAllErrorMessages();
        locationToggleSwitchLabel.innerText = "Enter Location (Optional):"
    }
}
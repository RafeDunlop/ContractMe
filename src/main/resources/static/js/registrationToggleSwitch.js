import {
    hideAllErrorMessages
} from "./locationFormValidation.js";

let locationToggleSwitch = document.getElementById("location-toggleswitch");
let locationForm = document.getElementById("location-form");
let locationToggleSwitchLabel = document.getElementById("location-toggle-switch-label");

let contractorToggleSwitch = document.getElementById("contractor-toggleswitch");
let contractorForm = document.getElementById("contractor-form");

locationToggleSwitch.addEventListener("click", displayLocationForm);
contractorToggleSwitch.addEventListener("click", displayContractorForm);

function displayLocationForm() {
    if (locationToggleSwitch.checked === true) {
        locationForm.style.display = "block";
    }
    else if (contractorToggleSwitch.checked === true) {
        locationToggleSwitch.checked = true;
        locationForm.style.display = "block";
    } else {
        locationForm.style.display = "none";
        hideAllErrorMessages();
    }
}

function displayContractorForm() {
    if (contractorToggleSwitch.checked === true) {
        locationToggleSwitch.checked = true;
        locationForm.style.display = "block";
        contractorForm.style.display = "block";
        locationToggleSwitchLabel.innerText = "Enter Location:"
    }
    else {
        locationToggleSwitch.checked = false;
        locationForm.style.display = "none";
        contractorForm.style.display = "none";
        hideAllErrorMessages();
        locationToggleSwitchLabel.innerText = "Enter Location (Optional):"
    }
}
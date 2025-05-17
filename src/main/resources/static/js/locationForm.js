import {
    hideAllErrorMessages
} from "./locationFormValidation.js";

let locationToggleSwitch = document.getElementById("location-toggleswitch");
let locationForm = document.getElementById("location-form");


locationToggleSwitch.addEventListener("click", displayLocationForm);


function displayLocationForm() {
    if (locationToggleSwitch.checked === true) {
        locationForm.style.display = "block";

    }
    else {
        locationForm.style.display = "none";
        hideAllErrorMessages()

    }
}

//postcodeField.addEventListener("blur", validatePostcode);
/*function validatePostcode() {
    let postcode = postcodeField.value.trim();
    let postcodePattern = /^[\p{L}\p{N} ]+$/u;
    let spaceCount = (postcode.match(/ /g) || []).length;

    if (!postcode) {
        postcodeFrontendErrorMessage.textContent = "Postcode cannot be empty.";
        postcodeFrontendError.hidden = false;
        postcodeFrontendErrorMessage.hidden = false;
        postcodeBackendError.hidden = true;
    } else if (!postcodePattern.test(postcode) || spaceCount > 1) {
        postcodeFrontendErrorMessage.textContent = "Postcode contains invalid characters.";
        postcodeFrontendError.hidden = false;
        postcodeFrontendErrorMessage.hidden = false;
        postcodeBackendError.hidden = true;
    } else {
        postcodeFrontendErrorMessage.textContent = "";
        postcodeFrontendError.hidden = true;
        postcodeFrontendErrorMessage.hidden = true;
        postcodeBackendError.hidden = true;
        }
    }*/


let locationToggleSwitch = document.getElementById("location-toggleswitch");
let locationForm = document.getElementById("location-form");
let addressField = document.getElementById("address");
let suburbField = document.getElementById("suburb");
let cityField = document.getElementById("city");

let postcodeField = document.getElementById("postcode");
let postcodeFrontendErrorMessage = document.getElementById("postcode-frontend-error-message");
let postcodeFrontendError = document.getElementById("postcode-frontend-error");
let postcodeBackendError = document.getElementById("postcode-backend-error");

let cityFrontendErrorMessage = document.getElementById("city-frontend-error-message");
let cityFrontendError = document.getElementById("city-frontend-error");
let cityBackendError = document.getElementById("city-backend-error");

let countryField = document.getElementById("country");

locationToggleSwitch.addEventListener("click", displayLocationForm);

postcodeField.addEventListener("input", validatePostcode);
cityField.addEventListener("input", validateCity);


function displayLocationForm() {
    if (locationToggleSwitch.checked === true) {
        locationForm.style.display = "block";
    }
    else {
        closeLocationForm()
    }
}

function closeLocationForm() {
    locationForm.style.display = "none";
    clearInputField(addressField);
    clearInputField(suburbField);
    clearInputField(cityField);
    clearInputField(postcodeField);
    clearInputField(countryField);
}
function clearInputField(inputField) {
    if (!(inputField === "")) {
        inputField.value = "";
    }
}

function validatePostcode() {
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
}

/**
 * Validates City Field.
 */
function validateCity() {
    let city = cityField.value.trim();
    let cityPattern = /^[\p{L} \-']+$/u;
    let spaceCount = (city.match(/ /g) || []).length;
    if (!city) {
        cityFrontendErrorMessage.textContent = "";
        cityFrontendError.hidden = true;
        cityFrontendErrorMessage.hidden = true;
        cityBackendError.hidden = true;
    } else if (!cityPattern.test(city) || spaceCount > 1) {
        cityFrontendErrorMessage.textContent = "City contains invalid characters.";
        cityFrontendError.hidden = false;
        cityBackendError.hidden = true;
    } else {
        cityFrontendErrorMessage.textContent = "";
        cityFrontendError.hidden = true;
        cityFrontendErrorMessage.hidden = true;
        cityBackendError.hidden = true;
    }
}

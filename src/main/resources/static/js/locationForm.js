let locationToggleSwitch = document.getElementById("location-toggleswitch");
let locationForm = document.getElementById("location-form");
let addressField = document.getElementById("address");
let suburbField = document.getElementById("suburb");
let cityField = document.getElementById("city");
let postcodeField = document.getElementById("postcode");
let countryField = document.getElementById("country");


locationToggleSwitch.addEventListener("click", displayLocationForm);

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
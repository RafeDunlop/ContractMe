let locationButton = document.getElementById("location-button");
let submitLocationButton = document.getElementById("submit-location");
let cancelLocationButton = document.getElementById("cancel-location");
let locationForm = document.getElementById("location-form");
let addressField = document.getElementById("address");
let suburbField = document.getElementById("suburb");
let cityField = document.getElementById("city");
let postcodeField = document.getElementById("postcode");
let countryField = document.getElementById("country");


locationButton.addEventListener("click",  displayLocationForm);
submitLocationButton.addEventListener("click", storeLocationDetails);
cancelLocationButton.addEventListener("click", closeLocationForm)

function displayLocationForm() {
    locationForm.style.display = "block";
}

function storeLocationDetails() {}

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
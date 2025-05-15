let locationToggleSwitch = document.getElementById("location-toggleswitch");
let locationForm = document.getElementById("location-form");
let addressField = document.getElementById("address");
let suburbField = document.getElementById("suburb");
let cityField = document.getElementById("city");
let postcodeField = document.getElementById("postcode");
let countryField = document.getElementById("country");
let autocompleteList = document.getElementById("autocomplete-list");

let localisation;
let autocompleteMap = new Map();
let timeoutId;
let committedFields = {
    address: addressField.value,
    suburb: suburbField.value,
    city: cityField.value,
    postcode: postcodeField.value,
    country: countryField.value
}
const inputDelayMS = 300;

locationToggleSwitch.addEventListener("click", displayLocationForm);

/** Js file used for autocompleting the tag entry field on viewRenovation.html */

document.addEventListener("DOMContentLoaded", getLocalisation);



// Listen for input events on the tag input field
addressField.addEventListener("input", function () {
    const input = addressField.value.trim();
    if (input.length) {
        updateAutocomplete(input)
    } else {
        autocompleteList.innerHTML = "";
    }
});

/**
 * Fetches the autocomplete suggestions for a address prompt then updates the UI.
 * @param {string} input - The input from the user.
 */
function updateAutocomplete(input) {
    clearTimeout(timeoutId);
    if (autocompleteMap.has(input)) { // don't wait if we already know what the answer is
        setAutoCompleteList(autocompleteMap.get(input))
    } else {
        timeoutId = setTimeout(async () => {
            setAutoCompleteList(await addAutocomplete(input, autocompleteMap))
        }, inputDelayMS)
    }
}

async function addAutocomplete(input) {
    try {
        const response = await fetch(`location/address-autocomplete/${encodeURIComponent(input)}`, {
            method: "POST",
            headers: {
                "X-CSRF-TOKEN": addressField.getAttribute("data-csrf"),
                "Content-Type": "application/json"
            },
            body: JSON.stringify(localisation)
        })
        const results = await response.json();
        autocompleteMap.set(input, results);
        return results;
    } catch (error) {
        console.error("Error retrieving address autocomplete options:", error);
    }
    return null;
}

async function getLocalisation() {
    try {
        const response = await fetch(`location/localisation`, {
            method: "GET",
            headers: {
                "X-CSRF-TOKEN": addressField.getAttribute("data-csrf"),
                "Content-Type": "application/json"
            }
        })
        localisation = await response.json();
    } catch (error) {
        console.error("Error retrieving localisation:", error)
    }
}

/**
 * Displays up to 3 tag suggestions in the autocomplete list.
 * @param addressList
 */
function setAutoCompleteList(addressList) {
    autocompleteList.innerHTML = "";
    if (addressList.length === 0) {
        const noAddressesMessage = document.createElement("li");
        noAddressesMessage.classList.add("list-group-item", "disabled");
        noAddressesMessage.textContent = "No location suggestions are available";
        autocompleteList.appendChild(noAddressesMessage);
        return;
    }

    for (let i = 0; i < addressList.length; i++) {
        autocompleteList.appendChild(getAutocompleteOption(addressList[i]));
    }
}

function getAutocompleteOption(address) {
    const item = document.createElement("li");
    item.classList.add("list-group-item");
    item.textContent = address.formatted;

    item.addEventListener("mouseover", () => {
        addressField.value = address.address_line1
        suburbField.value = address.region
        cityField.value = address.city
        postcodeField.value = address.postcode
        countryField.value = address.country
    })

    item.addEventListener("mouseout", () => {
        addressField.value = committedFields.address
        suburbField.value = committedFields.suburb
        cityField.value = committedFields.city
        postcodeField.value = committedFields.postcode
        countryField.value = committedFields.country
    })

    item.addEventListener("click", function () {
        committedFields.address = address.address_line1
        committedFields.suburb = address.region
        committedFields.city = address.city
        committedFields.postcode = address.postcode
        committedFields.country = address.country

        autocompleteList.innerHTML = "";
    });

    return item;
}

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
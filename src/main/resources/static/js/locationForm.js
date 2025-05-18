let locationToggleSwitch = document.getElementById("location-toggleswitch");
let locationForm = document.getElementById("location-form");
let addressField = document.getElementById("address");
let suburbField = document.getElementById("suburb");
let cityField = document.getElementById("city");
let postcodeField = document.getElementById("postcode");
let countryField = document.getElementById("country");

locationToggleSwitch.addEventListener("click", displayLocationForm);


/** Js file used for autocompleting the tag entry field on viewRenovation.html */

const inputDelayMS = 300;
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

document.addEventListener("DOMContentLoaded", getLocalisation);
/**
 * Hides the autocomplete options if you click away
 */
document.addEventListener("click", event => {
    if (addressField === event.target) {
        triggerUpdateAutocomplete()
    } else if (locationForm.contains(event.target)) {
        commitAddressFields()
        autocompleteList.innerHTML = "";
    } else {
        autocompleteList.innerHTML = "";
    }
})
addressField.addEventListener("input", triggerUpdateAutocomplete);

/**
 * Handles the logic of whether to update the autocomplete list (or hide it).
 * Calls function to commit the address details
 */
function triggerUpdateAutocomplete() {
    const input = addressField.value.trim();
    if (input.length) {
        updateAutocomplete(input)
    } else {
        autocompleteList.innerHTML = "";
    }
    commitAddressFields();
}

/**
 * Commits the address fields currently inputted to revert to
 * if an autocomplete option is hovered over but not clicked
 */
function commitAddressFields() {
    committedFields.address = addressField.value.trim();;
    committedFields.suburb = suburbField.value.trim();
    committedFields.city = cityField.value.trim();
    committedFields.postcode = postcodeField.value.trim();
    committedFields.country = countryField.value.trim();
}

/**
 * Updates/schedules new autocomplete options and cancels any previously scheduled updates.
 * Caches responses.
 * @param {string} input - The input from the user.
 */
function updateAutocomplete(input) {
    clearTimeout(timeoutId);
    if (autocompleteMap.has(input)) { // don't wait if we already know what the answer is
        setAutoCompleteList(autocompleteMap.get(input))
    } else {
        timeoutId = setTimeout(async () => {
            setAutoCompleteList(await addAutocomplete(input))
        }, inputDelayMS)
    }
}

/**
 * Gets the autocomplete options for a specified prompt, returns them and places them in {@code autocompleteMap}
 * @param input The input for which to retrieve autocomplete suggestions
 * @returns {Promise<Object[]>} returns a promise of the autocomplete suggestions, a list of addresses
 */
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

/**
 * Gets the localisation of the current client based on their IP address using Geoapify IP Geolocation API
 * and stores it in {@code localisation}
 * @returns {Promise<void>} A promise to execute this void function
 */
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
 * Resets the address autocomplete list tag and adds each address specified to it.
 * If the list is empty adds a "no location suggestions are available" list item instead
 * @param addressList The list of addresses to create list items for
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

/**
 * Makes a single list item tag corresponding to the specified address and configures its event listeners
 * @param address The address for which to create a list item tag
 * @returns {HTMLLIElement} The fully qualified list item tag to be appended to a list tag
 */
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



}
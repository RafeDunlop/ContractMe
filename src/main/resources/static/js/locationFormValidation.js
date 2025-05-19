const addressPattern = /^[\p{L}\-'\d\s.\/]*$/u;
const suburbPattern = /^[\p{L}\d\-'\s]*$/u;
const cityPattern = /^[\p{L}\-'\s]*$/u;
const postcodePattern = /^(?!.* {2})[\p{L}\p{N} ]*$/u;
const countryPattern = /^(?!.* {2})[\p{L}\-' ]*$/u;


let addressField = document.getElementById("address");
let addressFrontendError = document.getElementById("address-frontend-error");
let addressFrontendErrorMessage = document.getElementById("address-frontend-error-message");
let addressBackendError = document.getElementById("address-backend-error");

let suburbField = document.getElementById("suburb");
let suburbFrontendError = document.getElementById("suburb-frontend-error");
let suburbFrontendErrorMessage = document.getElementById("suburb-frontend-error-message");
let suburbBackendError = document.getElementById("suburb-backend-error")

let cityField = document.getElementById("city");
let cityFrontendError = document.getElementById("city-frontend-error");
let cityFrontendErrorMessage = document.getElementById("city-frontend-error-message");
let cityBackendError = document.getElementById("city-backend-error")

let postcodeField = document.getElementById("postcode");
let postcodeFrontendError = document.getElementById("postcode-frontend-error");
let postcodeFrontendErrorMessage = document.getElementById("postcode-frontend-error-message");
let postcodeBackendError = document.getElementById("postcode-backend-error")

let countryField = document.getElementById("country");
let countryFrontendError = document.getElementById("country-frontend-error");
let countryFrontendErrorMessage = document.getElementById("country-frontend-error-message");
let countryBackendError = document.getElementById("country-backend-error")


document.addEventListener("DOMContentLoaded", () => {



    // Event listeners
    ["input", "focus"].forEach(event =>
        addressField.addEventListener(event, () =>
            checkOptionalField(addressField.value,
                addressFrontendError,
                addressFrontendErrorMessage,
                "Street address contains invalid characters.",
                addressBackendError,
                addressPattern
            )
        )
    );


    ["input", "focus"].forEach(event =>
        suburbField.addEventListener(event, () =>
        checkOptionalField(suburbField.value,
            suburbFrontendError,
            suburbFrontendErrorMessage,
            "Suburb contains invalid characters",
            suburbBackendError,
            suburbPattern
            )
        )
    );

    ["input", "focus"].forEach(event =>
        cityField.addEventListener(event, () =>
        checkOptionalField(cityField.value,
            cityFrontendError,
            cityFrontendErrorMessage,
            "City contains invalid characters",
            cityBackendError,
            cityPattern
            )
        )
    );

    ["input", "focus"].forEach(event =>
    postcodeField.addEventListener(event, () =>
        checkOptionalField(postcodeField.value,
            postcodeFrontendError,
            postcodeFrontendErrorMessage,
            "Postcode contains invalid characters",
            postcodeBackendError,
            postcodePattern
            )
        )
    );

    ["input", "focus"].forEach(event =>
        countryField.addEventListener(event, () =>
        checkOptionalField(countryField.value,
            countryFrontendError,
            countryFrontendErrorMessage,
            "Country contains invalid characters",
            countryBackendError,
            countryPattern
            )
        )
    );

});

/**
 * Checks suburb, city, postcode or country fields to run frontend validation.
 * @param input text inside input field
 * @param frontendError associated front end error box
 * @param frontendErrorMessage message box of associated front end error
 * @param errorMessageContent text string of error message
 * @param backendError backend error box
 * @param fieldPattern regex against which the user input is matched
 */
function checkOptionalField(input, frontendError, frontendErrorMessage, errorMessageContent, backendError, fieldPattern) {
    input = input.trim();

    if (!fieldPattern.test(input)) {
        frontendErrorMessage.textContent = errorMessageContent;
        frontendError.hidden = false;
        frontendErrorMessage.hidden = false;
        backendError.hidden = true;

    }
    else {
        hideSingleErrorMessage(frontendErrorMessage, frontendError, backendError)
    }
}

/**
 * Hides all error messages on the location form. This is used so that, if the
 * user has any frontend or backend error messages on the location form and closes
 * then reopens it with the toggle switch, the error messages disappear.
 */
export function hideAllErrorMessages() {
    hideSingleErrorMessage(suburbFrontendErrorMessage, suburbFrontendError, suburbBackendError);
    hideSingleErrorMessage(cityFrontendErrorMessage, cityFrontendError, cityBackendError);
    hideSingleErrorMessage(postcodeFrontendErrorMessage, postcodeFrontendError, postcodeBackendError);
    hideSingleErrorMessage(countryFrontendErrorMessage, countryFrontendError, countryBackendError);

}

/**
 * Closes an individual error field.
 * @param frontendErrorMessage message box of associated front end error
 * @param frontendError associated front end error box
 * @param backendError backend error box
 */
function hideSingleErrorMessage(frontendErrorMessage, frontendError, backendError) {
    frontendErrorMessage.textContent = "";
    frontendError.hidden = true;
    frontendErrorMessage.hidden = true;
    backendError.hidden = true;
}

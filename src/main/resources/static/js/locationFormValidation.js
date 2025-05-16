const suburbPattern = /^[\p{L}\d\-'\s]*$/u;

document.addEventListener("DOMContentLoaded", () => {

    let addressField = document.getElementById("address");

    let suburbField = document.getElementById("suburb");
    let suburbFrontendError = document.getElementById("suburb-frontend-error");
    let suburbFrontendErrorMessage = document.getElementById("suburb-frontend-error-message");
    let suburbBackendError = document.getElementById("suburb-backend-error")

    let cityField = document.getElementById("city");

    let postcodeField = document.getElementById("postcode");

    let countryField = document.getElementById("country");

    // Event listeners
    suburbField.addEventListener("input", () =>
        checkOptionalField(suburbField.value,
            suburbFrontendError,
            suburbFrontendErrorMessage,
            "Suburb must only include letters, spaces, hyphens, digits or apostrophes.",
            suburbBackendError,
            suburbPattern
            )
    );

});

function checkOptionalField(input, frontendError, frontendErrorMessage, errorMessageContent, backendError, fieldPattern) {
    input = input.trim();

    if (!fieldPattern.test(input)) {
        frontendErrorMessage.textContent = errorMessageContent;
        frontendError.hidden = false;
        frontendErrorMessage.hidden = false;
        backendError.hidden = true;

    }

    else {
        frontendErrorMessage.textContent = "";
        frontendError.hidden = true;
        frontendErrorMessage.hidden = true;
        backendError.hidden = true;
    }
}
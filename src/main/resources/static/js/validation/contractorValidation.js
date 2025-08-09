const phonePattern = /^[\d\s]+$/;

/**
 * Validates the given hourly rate and updates the frontend error messages and visibility accordingly.
 *
 * @param {string|number} hourlyRate - The hourly rate to validate.
 * @param {HTMLElement} hourlyRateFrontendErrorMessage - Element to display the error message for invalid hourly rate.
 * @param {HTMLElement} hourlyRateFrontendError - Element to control visibility of frontend error indicator.
 * @param {HTMLElement} hourlyRateBackendError - Element to control visibility of backend error.
 */
export function validateHourlyRate(hourlyRate, hourlyRateFrontendErrorMessage, hourlyRateFrontendError,
                                   hourlyRateBackendError) {
    let hourlyRateNum = Number(hourlyRate);
    if (hourlyRateBackendError) hourlyRateBackendError.hidden = true;
    if (isNaN(hourlyRateNum) || hourlyRateNum < 0) {
        hourlyRateFrontendErrorMessage.textContent = "Invalid hourly rate";
        hourlyRateFrontendError.hidden = false;
        hourlyRateFrontendErrorMessage.hidden = false;
    } else {
        hourlyRateFrontendErrorMessage.textContent = "";
        hourlyRateFrontendError.hidden = true;
        hourlyRateFrontendErrorMessage.hidden = true;
    }
}

/**
 * Validates the provided phone number and updates the corresponding error messages and states.
 *
 * @param {string} phoneNumber - The phone number input to validate.
 * @param {HTMLElement} phoneNumberFrontendErrorMessage - The HTML element to display frontend error messages.
 * @param {HTMLElement} phoneNumberFrontendError - The HTML element to visually represent a frontend error.
 * @param {HTMLElement} phoneNumberBackendError - The HTML element to visually represent a backend error.
 * */
export function validatePhoneNumber(phoneNumber, phoneNumberFrontendErrorMessage, phoneNumberFrontendError,
                                    phoneNumberBackendError) {
    phoneNumber = phoneNumber.trim();
    if (phoneNumberBackendError) phoneNumberBackendError.hidden = true;
    if (phoneNumber === "") {
        phoneNumberFrontendErrorMessage.textContent = "You must enter a valid phone number";
        phoneNumberFrontendError.hidden = false;
        phoneNumberFrontendErrorMessage.hidden = false;
    } else if (phoneNumber.length < 8 || phoneNumber.length > 15 || !phonePattern.test(phoneNumber)) {
        phoneNumberFrontendErrorMessage.textContent = "Your phone number is invalid";
        phoneNumberFrontendError.hidden = false;
        phoneNumberFrontendErrorMessage.hidden = false;
    } else {
        phoneNumberFrontendErrorMessage.textContent = "";
        phoneNumberFrontendErrorMessage.hidden = true;
        phoneNumberFrontendError.hidden = true
    }
}

/**
 * Validates the provided country code and updates the frontend or backend error messages accordingly.
 *
 * @param {number} countryCode - The country code to validate. Must be a number between 1 and 999.
 * @param {HTMLElement} countryCodeFrontendErrorMessage - The HTML element where an error message is displayed for invalid country codes.
 * @param {HTMLElement} countryCodeFrontendError - The HTML element to showcase or hide the frontend error state.
 * @param {HTMLElement} countryCodeBackendError - The HTML element representing a backend error state, which will be hidden during the validation.
 */
export function validateCountryCode(countryCode, countryCodeFrontendErrorMessage, countryCodeFrontendError,
                                    countryCodeBackendError) {
    if (countryCodeBackendError) countryCodeBackendError.hidden = true;
    if (countryCode <= 0 || countryCode > 999) {
        countryCodeFrontendErrorMessage.textContent = "Invalid country code";
        countryCodeFrontendError.hidden = false;
        countryCodeFrontendErrorMessage.hidden = false;
    } else {
        countryCodeFrontendErrorMessage.textContent = "";
        countryCodeFrontendError.hidden = true;
        countryCodeFrontendErrorMessage.hidden = true;
    }
}
const phonePattern = /\d+/;

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
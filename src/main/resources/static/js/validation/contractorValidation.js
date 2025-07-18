const phonePattern = /\d+/;

export function validateHourlyRate(hourlyRate, hourlyRateFrontendErrorMessage, hourlyRateFrontendError,
                                   hourlyRateBackendError) {
    let hourlyRateNum = Number(hourlyRate);
    if (isNaN(hourlyRateNum) || hourlyRateNum < 0) {
        hourlyRateFrontendErrorMessage.textContent = "Invalid hourly rate";
        hourlyRateFrontendError.hidden = false;
        hourlyRateFrontendErrorMessage.hidden = false;
        hourlyRateBackendError.hidden = true;
    } else {
        hourlyRateFrontendErrorMessage.textContent = "";
        hourlyRateFrontendError.hidden = true;
        hourlyRateFrontendErrorMessage.hidden = true;
        hourlyRateBackendError.hidden = true;
    }
}

export function validatePhoneNumber(phoneNumber, phoneNumberFrontendErrorMessage, phoneNumberFrontendError,
                                    phoneNumberBackendError) {
    phoneNumber = phoneNumber.trim();

    if (phoneNumber === "") {
        phoneNumberFrontendErrorMessage.textContent = "You must enter a valid phone number";
        phoneNumberFrontendError.hidden = false;
        phoneNumberFrontendErrorMessage.hidden = false;
        phoneNumberBackendError.hidden = true;
    } else if (phoneNumber.length < 8 || phoneNumber.length > 15 || !phonePattern.test(phoneNumber)) {
        phoneNumberFrontendErrorMessage.textContent = "Your phone number is invalid";
        phoneNumberFrontendError.hidden = false;
        phoneNumberFrontendErrorMessage.hidden = false;
        phoneNumberBackendError.hidden = true;
    } else {
        phoneNumberFrontendErrorMessage.textContent = "";
        phoneNumberBackendError.hidden = true;
        phoneNumberFrontendErrorMessage.hidden = true;
        phoneNumberFrontendError.hidden = true
    }
}
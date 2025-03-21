/**
 * Validates a given input field based off the provided regular expression pattern
 * If the input is valid the error label is hidden, if it isn't the appropriate error message is displayed
 * @param input in the form field currently
 * @param pattern the regular expression to test the input against
 * @param errorLabel the error label to display any errors
 * @param errorMessage the message to be displayed in the label
 * @returns {boolean} true if the input is valid, false otherwise
 */
export function validateField(input, pattern, errorLabel, errorMessage) {
    let isValidInput = pattern.test(input)
    if (isValidInput) {
        errorLabel.hidden = true;
        return true;
    } else {
        errorLabel.hidden = false;
        errorLabel.textContent = errorMessage;
        return false;
    }
}
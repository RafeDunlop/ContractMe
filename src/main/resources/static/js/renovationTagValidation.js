
let tagInput = document.getElementById("tagName");
let tagFrontendError = document.getElementById("tag-frontend-error");
let tagFrontendErrorMessage = document.getElementById("tag-frontend-error-message");
let tagBackendError = document.getElementById("tag-backend-error");

const tagNameListElements = document.querySelectorAll('#tag-list span');
const tagNamesList = Array.from(tagNameListElements).map(el => el.textContent.trim());


const tagPattern = /^(?=.*\p{L}).*$/u;

tagInput.addEventListener("input", function () {validateTag(tagInput.value)})

function validateTag(input) {
    input = input.trim().toLowerCase();
    if (tagNamesList.length >= 5) {
        tagFrontendErrorMessage.textContent = "Renovation cannot have more than 5 tags.";
        tagFrontendError.hidden = false;
        tagBackendError.hidden = true;
        return false;
    } else if (tagNamesList.includes(input)) {
        tagFrontendErrorMessage.textContent = "Renovation cannot contain duplicate tag names.";
        tagFrontendError.hidden = false;
        tagBackendError.hidden = true;
        return false;
    } else if (input.length > 128) {
        tagFrontendErrorMessage.textContent = "Tag cannot be greater than 128 characters.";
        tagFrontendError.hidden = false;
        tagBackendError.hidden = true;
        return false;
    } else if (input === "" || !tagPattern.test(input)) {
        tagFrontendErrorMessage.textContent = "Tags must contain one or more letters.";
        tagFrontendError.hidden = false;
        tagBackendError.hidden = true;
        return false;
    } else {
        tagFrontendErrorMessage.textContent = "";
        tagFrontendError.hidden = true;
        tagBackendError.hidden = true;
        return true;
    }


}
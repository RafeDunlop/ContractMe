let tagNameInput;
let tagFrontendError = document.getElementById("tag-frontend-error");
let tagFrontendErrorMessage = document.getElementById("tag-frontend-error-message");
let tagBackendError = document.getElementById("tag-backend-error");
let tagNameListElements
let tagNamesList


if (formPath === "/renovations/view") {
    tagNameInput = document.getElementById("tagName");
    tagNameListElements = document.querySelectorAll('#tag-list span');
    tagNamesList = Array.from(tagNameListElements).map(el => el.textContent.trim());
} else if (formPath === "/renovations/search") {
    tagNameInput = document.getElementById("tag-input");
} else {
    console.warn(`Unexpected form path: ${formPath}`);
}

const tagPattern = /^(?=.*\p{L}).*$/u;

tagNameInput.addEventListener("input", function () {validateTag(tagNameInput.value)})

/**
 * Validates a tag input for renovations.
 *
 * Displays appropriate frontend error messages for any validation failures.
 *
 * @param {string} input - The tag name entered by the user.
 * @returns {Promise<boolean>} - Returns a promise that resolves to `true` if validation passes, otherwise `false`.
 *
 */
async function validateTag(input) {
    input = input.trim().toLowerCase();
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const errors = [];
    if (formPath === "/renovations/view" && tagNamesList.length >= 5) {
        errors.push("Renovation cannot have more than 5 tags.");
    } else if (formPath === "/renovations/view" && tagNamesList.includes(input)) {
        errors.push("Renovation cannot contain duplicate tag names.");
    } else {
        if (input === "" || !tagPattern.test(input)) {
            errors.push("Tags must contain one or more letters.");
        } if (input.length > 128) {
            errors.push("Tag cannot be greater than 128 characters.");
        }
        const response = await fetch(`/renovations/tags/profanity-filter?tagName=${encodeURIComponent(input)}`, {
            method: "GET",
            headers: {
                'X-CSRF-TOKEN': csrfToken,
                'Content-Type': 'application/json'
            },
        });
        const profanityInTag = await response.json();
        if (profanityInTag === true) {
            errors.push("Name does not follow the system language standards.")
        }
    }

    if (errors.length) {
        tagFrontendErrorMessage.innerHTML =
            errors.map(msg => `<li>${msg}</li>`).join("");
        tagFrontendError.classList.add("show");    // bootstrap’s .show or just remove hidden
        tagFrontendError.hidden = false;
        tagBackendError.hidden = true;
        return false;
    } else {
        tagFrontendErrorMessage.innerHTML = "";
        tagFrontendError.hidden = true;
        tagBackendError.hidden = true;
        return true;
    }
}
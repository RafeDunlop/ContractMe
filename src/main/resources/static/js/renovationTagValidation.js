
let tagInput = document.getElementById("tagName");
let tagFrontendError = document.getElementById("tag-frontend-error");
let tagFrontendErrorMessage = document.getElementById("tag-frontend-error-message");
let tagBackendError = document.getElementById("tag-backend-error");

const tagNameListElements = document.querySelectorAll('#tag-list span');
const tagNamesList = Array.from(tagNameListElements).map(el => el.textContent.trim());


const tagPattern = /^(?=.*\p{L}).*$/u;

tagInput.addEventListener("input", function () {validateTag(tagInput.value)})

async function validateTag(input) {
    input = input.trim().toLowerCase();
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const errors = [];
    if (tagNamesList.length >= 5) {
        errors.push("Renovation cannot have more than 5 tags.");
    } else if (tagNamesList.includes(input)) {
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
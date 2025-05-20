/**
 * Tag search bar input logic, for search renovations html page.
 * Allows users to input tags, displays them as bubbles inside search bar, and adds hidden inputs for form submission.
 */

// DOM Elements
const tagInput = document.getElementById("tag-input");
const tagBubbles = document.getElementById("tag-bubbles");
const tagInputContainer = document.getElementById("tag-input-container");
const hiddenInputs = document.getElementById("hidden-tag-inputs");

// Frontend list for tags currently in search bar
let tags = [];


/**
 * Focuses the tag input field.
 */
function focusTagInput() {
    tagInput.focus();
}

/**
 * Adds a tag to the tag list and displays on UI.
 * @param {string} tag The tag name to add.
 */
function addTag(tag) {
    tag = tag.trim();
    if (tagFrontendError.hidden === true) {
        if (tag === "" || tags.includes(tag)) return;
        tags.push(tag);

        // Create bubble
        const bubble = document.createElement("span");
        bubble.className = "tag-box badge bg-success d-flex align-items-center me-2 mt-1 mb-1";

        const bubbleText = document.createElement("span");
        bubbleText.className = "tag-text-small text-truncate";
        bubbleText.textContent = tag;

        // Remove button
        const closeBtn = document.createElement("button");
        closeBtn.className = "tag-delete-button-small btn-sm border-0 bg-transparent text-light ms-1";
        closeBtn.style.cursor = "pointer";
        closeBtn.innerHTML = "&times;";
        closeBtn.onclick = () => removeTag(tag, bubble);

        bubble.appendChild(bubbleText);
        bubble.appendChild(closeBtn);
        tagInputContainer.insertBefore(bubble, tagBubbles);

        // Add hidden input
        const hidden = document.createElement("input");
        hidden.type = "hidden";
        hidden.name = "tagNameList";
        hidden.value = tag;
        hidden.dataset.tag = tag;
        hiddenInputs.appendChild(hidden);
    }
}


/**
 * Removes a tag from the tag list and UI.
 * @param {string} tag the tag to remove from the search bar.
 * @param {HTMLElement} bubbleElement the bubble element representing the tag.
 */
function removeTag(tag, bubbleElement) {
    tags = tags.filter(t => t !== tag);
    bubbleElement.remove();
    const hidden = hiddenInputs.querySelector(`input[data-tag="${tag}"]`);
    if (hidden) hidden.remove();
}

/**
 * Handles key presses and on enter adds the tag to be displayed in the search bar instead of submitting form.
 * Change this to instead add tag to search bar via autocomplete, when that task is implemented this may not be needed.
 */
tagInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
        e.preventDefault();
        addTag(tagInput.value);
        tagInput.value = "";
        resetAutocomplete();
    } else if (e.key === "Backspace" && tagInput.value === "") {
        const lastTag = tags[tags.length - 1];
        if (lastTag) {
            const bubble = [...tagInputContainer.children].find(
                el => el.textContent.startsWith(lastTag)
            );
            removeTag(lastTag, bubble);
        }
    }
});
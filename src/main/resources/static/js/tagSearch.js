/**
 * Tag search bar input logic, for search renovations html page.
 * Allows users to input tags, displays them as bubbles inside search bar, and adds hidden inputs for form submission.
 */

// DOM Elements
const tagInput = document.getElementById("tag-input");
const tagInputContainer = document.getElementById("tag-input-container");
const hiddenInputs = document.getElementById("hidden-tag-inputs");

// Frontend list for tags currently in search bar
let tags = [];
let currentTabIndex = -1
/**
 * Focuses the tag input field.
 */
function focusTagInput() {
    tagInput.focus();
}

tagInput.addEventListener("input", function () {
    const partialTag = tagInput.value.trim();
    if (partialTag.length < 1) {
        resetAutocomplete();
        return
    }
    updateAutocomplete(partialTag);
});

/**
 * Updates the autocomplete list according to input.
 * @param partialTag - The input of the user.
 */
function updateSearchAutocomplete(partialTag) {
    const filteredTags = tags.map(t => t.trim());
    fetch(`/renovations/tags/autocomplete?partialTag=${encodeURIComponent(partialTag)}`)
    .then(response => response.json())
    .then(results => {
        const suggestions = results.filter(tag => !filteredTags.includes(tag));
        setSearchAutoCompleteList(suggestions);
    })
    .catch(error => console.error("Autocomplete fetch failed:", error));
}



/**
 * Displays up to 3 tag suggestions in the autocomplete list.
 * @param {string[]} tags - The list of tag names returned from the backend.
 */
function setSearchAutoCompleteList(tags) {
    const list = document.getElementById("autocomplete-list");

    list.innerHTML = "";

    // When currently no tags match display the message in place of dropdown
    if (tags.length === 0) {
        // No matching tags
        const noTagsMessage = document.createElement("li");
        noTagsMessage.classList.add("list-group-item", "disabled");
        noTagsMessage.textContent = "No matching tags";
        list.appendChild(noTagsMessage);
        return;
    }

    // Loop through each tag with max of 3 and create <li> for each
    for (let i = 0; i < Math.min(tags.length, 3); i++) {
        const tag = tags[i];
        const item = document.createElement("li");
        item.classList.add("list-group-item", "autocomplete-item");
        item.textContent = tag;
        item.tabIndex = 0;

        item.addEventListener("click", function () {
            resetAutocomplete();
            tagInput.value = "";
            addTag(tag)

        });

        item.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                item.click();
            }
        });

        list.appendChild(item);
    }
    currentTabIndex = -1;

}

/**
 * Clears the autocomplete display list.
 */
function resetAutocomplete() {
    document.getElementById("autocomplete-list").innerHTML = "";
}

/**
 * Adds a tag to the tag list and displays on UI.
 * @param {string} tag The tag name to add.
 */
function addTag(tag) {
    tag = tag.trim();
    if (tag === "" || tags.includes(tag)) return;
    tags.push(tag);

    // Create bubble
    const bubble = document.createElement("span");
    bubble.className = "badge bg-primary text-white me-1 mb-1";
    bubble.textContent = tag;

    // Remove button
    const closeBtn = document.createElement("span");
    closeBtn.className = "ms-1";
    closeBtn.style.cursor = "pointer";
    closeBtn.innerHTML = "&times;";
    closeBtn.onclick = () => removeTagByName(tag);

    bubble.appendChild(closeBtn);
    tagInputContainer.insertBefore(bubble, tagInput);

    // Add hidden input
    const hidden = document.createElement("input");
    hidden.type = "hidden";
    hidden.name = "tagNameList";
    hidden.value = tag;
    hidden.dataset.tag = tag;
    hiddenInputs.appendChild(hidden);
}

document.addEventListener("keydown", function (event) {
    const listItems = document.querySelectorAll("#autocomplete-list .autocomplete-item");

    if (listItems.length === 0) {
        return;
    }

    if (event.key === "ArrowDown") {
        event.preventDefault();
        currentTabIndex++;
        if (currentTabIndex >= listItems.length) {
            currentTabIndex = 0;
        }
        listItems[currentTabIndex].focus();
    }

    if (event.key === "ArrowUp") {
        event.preventDefault();
        currentTabIndex--;
        if (currentTabIndex < 0) {
            currentTabIndex = listItems.length - 1;
        }
        listItems[currentTabIndex].focus();
    }
});


/**
 * Handles removal of tag UI elements and corresponding hidden inputs.
 * Used by both tag input bubbles and green tag badges.
 */

function removeTagByName(tagName) {
    const hiddenInputs = document.getElementById("hidden-tag-inputs");
    const hiddenInput = hiddenInputs.querySelector(`input[data-tag="${tagName}"], input[value="${tagName}"]`);
    if (hiddenInput) hiddenInput.remove();

    const bubble = [...document.getElementById("tag-input-container").children]
        .find(el => el.textContent.trim().startsWith(tagName));
    if (bubble) bubble.remove();

    const greenTags = document.querySelectorAll(".tag-box");
    greenTags.forEach(tagElement => {
        const textSpan = tagElement.querySelector(".tag-text-small");
        if (textSpan && textSpan.textContent.trim() === tagName) {
            tagElement.remove();
        }
    });

    if (typeof tags !== 'undefined') {
        tags = tags.filter(t => t !== tagName);
    }
}

/**
 * Handles key presses and on enter adds the tag to be displayed in the search bar instead of submitting form.
 * Change this to instead add tag to search bar via autocomplete, when that task is implemented this may not be needed.
 */
tagInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" || e.key === ",") {
        e.preventDefault();
        addTag(tagInput.value);
        tagInput.value = "";
    } else if (e.key === "Backspace" && tagInput.value === "") {
        const lastTag = tags[tags.length - 1];
        if (lastTag) {
            removeTagByName(lastTag);
        }
    }
});

function setupTagSearch() {
    const tagInput = document.getElementById("tag-input");
    tagInput.addEventListener("keydown", function (e) {
        if (e.key === 'Enter' || e.key === ',') {
            e.preventDefault();
            const tagValue = tagInput.value.trim();
            if (tagValue) {
                addTag(tagValue);
                tagInput.value = "";
            }
        }
    });

    const initialTags = window.initialTags || [];
    initialTags.forEach(tag => {
        if (tag) addTag(tag);
    });


    tagInput.addEventListener("input", function () {
        const partial = tagInput.value.trim();
        if (partial.length < 1) {
            resetAutocomplete();
        } else {
            updateSearchAutocomplete(partial);
        }
    });

    document.addEventListener("click", function (e) {
        if (!tagInputContainer.contains(e.target)) {
            resetAutocomplete();
        }
    });
}


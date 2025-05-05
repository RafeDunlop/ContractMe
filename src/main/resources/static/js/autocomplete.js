/** Js file used for autocompleting the tag entry field on viewRenovation.html */

const input = document.getElementById("tagName");

// Listen for input events on the tag input field
input.addEventListener("input", function () {
    const partialTag = input.value.trim();
    if (partialTag.length < 1) {
        resetAutocomplete();
        return
    }
    updateAutocomplete(partialTag);
});

/**
 * Fetches the autocomplete suggestions for a partial tag input then updates the UI.
 * @param {string} partialTag - The partial input from the user.
 */
function updateAutocomplete(partialTag) {
    fetch(`/renovations/tags/autocomplete?partialTag=${encodeURIComponent(partialTag)}`)
        .then(response => response.json())
        .then(tags => {
            setAutoCompleteList(tags)
        })
        .catch(error => {
            console.error("Error getting tags:", error);
        });
    }

/**
 * Displays up to 3 tag suggestions in the autocomplete list.
 * @param {string[]} tags - The list of tag names returned from the backend.
 */
function setAutoCompleteList(tags) {
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
        item.classList.add("list-group-item");
        item.textContent = tag;

        item.addEventListener("click", function () {
            input.value = tag;
            resetAutocomplete();

            // Submit the form on autocomplete
            document.getElementById("add-tag-form").submit();

        });

        list.appendChild(item);
    }
}


/**
 * Clears the autocomplete display list.
 */
function resetAutocomplete() {
    document.getElementById("autocomplete-list").innerHTML = "";
}



/** Js file used for autocompleting the tag entry field on viewRenovation.html */
let currentTabIndex = -1;
const input = document.getElementById("tagName");


// Listen for input events on the tag input field
if (input) {
    input.addEventListener("input", function () {
        const partialTag = input.value.trim();
        if (partialTag.length < 1) {
            resetAutocomplete();
            return
        }
        updateAutocomplete(partialTag);
    });
}

// Event for navigating the autocomplete with the up/down arrow keys
document.addEventListener("keydown", function (event) {
    const listItems = document.querySelectorAll("#autocomplete-list .list-group-item:not(.disabled)");

   scrollThroughAutoComplete(listItems);
});

/**
 * Fetches the autocomplete suggestions for a partial tag input then updates the UI.
 * Removes tags that have been added to the renovation from the list of suggestions.
 * @param {string} partialTag - The partial input from the user.
 */
function updateAutocomplete(partialTag) {
    const existingTags = Array.from(document.getElementsByClassName("tag-text-large"))
        .map(element => element.textContent.trim());
    fetch(`renovations/tags/autocomplete?partialTag=${encodeURIComponent(partialTag)}`)
        .then(response => response.json())
        .then(tags => {
            const filteredTags = tags.filter(tag => !existingTags.includes(tag));
            setAutoCompleteList(filteredTags)
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
        item.setAttribute("tabindex", "0");

        // Event for submitting the tag clicked on
        item.addEventListener("click", function () {
            input.value = tag;
            resetAutocomplete();

            // Submit the form on autocomplete
            document.getElementById("add-tag-form").submit();

        });

        // Event for submitting the current tag selected in dropdown from enter press
        item.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                input.value = tag;
                resetAutocomplete();
                document.getElementById("add-tag-form").submit();
            }
        });

        list.appendChild(item);
    }
}


/**
 * Clears the autocomplete display list.
 */
function resetAutocomplete() {
    document.getElementById("autocomplete-list").innerHTML = "";
    currentTabIndex = -1;
}

export function scrollThroughAutoComplete(listItems) {
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
}

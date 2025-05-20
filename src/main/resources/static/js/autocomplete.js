/** Js file used for autocompleting the tag entry field on viewRenovation.html */

let formPath = window.location.pathname;
let input;

if (formPath === "/renovations/view") {
    input = document.getElementById("tagName");
} else if (formPath === "/renovations/search") {
    input = document.getElementById("tag-input");
} else {
    console.warn(`Unexpected form path: ${formPath}`);
}


// Listen for input events on the tag input field
input.addEventListener("input", function () {
    const partialTag = input.value.trim();
    if (partialTag.length < 1) {
        resetAutocomplete();
        return
    }
    updateAutocomplete(partialTag);
});

//Listens for if the user clicks on the page to dismiss the
//autocomplete suggestions
document.addEventListener("click", function () {

    resetAutocomplete();
});

/**
 * Fetches the autocomplete suggestions for a partial tag input then updates the UI.
 * Removes tags that have been added to the renovation from the list of suggestions.
 * @param {string} partialTag - The partial input from the user.
 */
function updateAutocomplete(partialTag) {
    let existingTags;
    if (input.id === "tagName") {
        existingTags = Array.from(document.getElementsByClassName("tag-text-large"))
            .map(element => element.textContent.trim());
    } else {
        existingTags = Array.from(document.getElementsByClassName("tag-text-large"))
            .map(element => element.textContent.trim());
    }
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

        item.addEventListener("click", function () {
            input.value = tag;
            resetAutocomplete();

            if (formPath === "/renovations/view") {
                document.getElementById("add-tag-form").action = "/renovations/addTag";
                document.getElementById("add-tag-form").submit();

            } else if (formPath === "/renovations/search") {
                addTag(input.value);
                document.getElementById("tag-input").value="";
                resetAutocomplete();
            }


        })

        list.appendChild(item);
    }
}


/**
 * Clears the autocomplete display list.
 */
function resetAutocomplete() {
    document.getElementById("autocomplete-list").innerHTML = "";
}

const input = document.getElementById("tagName");
input.addEventListener("input", function () {
    const partialTag = input.value.trim();
    if (partialTag.length < 1) {
        resetAutocomplete();
        return
    }
    updateAutocomplete(partialTag);
});




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


function setAutoCompleteList(tags) {
    const list = document.getElementById("autocomplete-list");

    list.innerHTML = "";

    // Loop through each tag with max of 3 and create <li> for each
    for (let i = 0; i < Math.min(tags.length, 3); i++) {
        const tag = tags[i];
        const item = document.createElement("li");
        item.classList.add("list-group-item");
        item.textContent = tag;

        item.addEventListener("click", function () {
            input.value = tag;
            resetAutocomplete();
        });

        list.appendChild(item);
    }
}



function resetAutocomplete() {
    document.getElementById("autocomplete-list").innerHTML = "";
}

updateAutocomplete()

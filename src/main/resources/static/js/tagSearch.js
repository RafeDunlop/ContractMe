const tagInput = document.getElementById("tag-input");
const tagInputContainer = document.getElementById("tag-input-container");
const hiddenInputs = document.getElementById("hidden-tag-inputs");

let tags = [];

function focusTagInput() {
    tagInput.focus();
}

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
    closeBtn.onclick = () => removeTag(tag, bubble);

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

function removeTag(tag, bubbleElement) {
    tags = tags.filter(t => t !== tag);
    bubbleElement.remove();
    const hidden = hiddenInputs.querySelector(`input[data-tag="${tag}"]`);
    if (hidden) hidden.remove();
}

tagInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" || e.key === ",") {
        e.preventDefault();
        addTag(tagInput.value);
        tagInput.value = "";
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

document.getElementById("tag-search-form").addEventListener("submit", function(e) {
    const tags = document.querySelectorAll('input[name="tagNameList"]');
    if (tags.length === 0) {
        e.preventDefault();
    }
});
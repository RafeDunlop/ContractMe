const skillsSelect= document.getElementById("skills-select");
const skillInputContainer= document.getElementById("skill-input-container");
const hiddenSkillInputContainer = document.getElementById("hidden-skill-input-container");
const validSkills = Array.from(skillsSelect.options).map(skill => skill.value);

let selectedSkills = []

/**
 * A listener to add a skill to the list of selected skills if it hasn't already been added when an option is pressed on
 * the selection box. Calls the method to create the bubble when a skill is added.
 */
skillsSelect.addEventListener("change", () => {
    const selectedSkill = skillsSelect.value.trim();
    const selectedOption = skillsSelect.selectedOptions[0];
    const displayName = selectedOption?.getAttribute("display-name") ?? selectedSkill;
    if (validSkills.includes(selectedSkill)) {
        if (!selectedSkills.includes(selectedSkill)) {
            selectedSkills.push(selectedSkill);
            createSkillBubble(selectedSkill, displayName);
        }
    }
    if (selectedSkill !== "Add Skills") {
        skillsSelect.style.color = "black";
    }
});

/**
 * Creates the skill bubble underneath the selection field with a close button. Removes the skill from the selected skills
 * list if the close button is pressed.
 * @param selectedSkill The enum value of the skill
 * @param displayName The string associated with the skill enum value
 */
function createSkillBubble(selectedSkill, displayName) {
    const bubble = document.createElement("span");
    bubble.className = "tag-box badge bg-success d-inline-flex align-items-center me-2 mb-2";

    const bubbleText = document.createElement("span");
    bubbleText.className = "tag-text-large ms-1 text-truncate";
    bubbleText.textContent = displayName;
    bubble.appendChild(bubbleText);

    const deleteButton = document.createElement("button");
    deleteButton.className = "tag-delete-button-large btn-sm border-0 bg-transparent text-light ms-1";
    deleteButton.textContent = "X";
    deleteButton.addEventListener("click", () => {
        deleteSkill(deleteButton, bubble, selectedSkill);
    });
    bubble.appendChild(deleteButton);

    skillInputContainer.appendChild(bubble);

    const hiddenSkillInput = document.createElement("input");
    hiddenSkillInput.type = "hidden";
    hiddenSkillInput.name = "skills";
    hiddenSkillInput.id = "hidden" + selectedSkill;
    hiddenSkillInput.value = selectedSkill;

    hiddenSkillInputContainer.appendChild(hiddenSkillInput);
}

/**
 * Delete skill bubble when delete button is pressed.
 * @param deleteButton Button getting the event listener
 * @param bubble Skill bubble to be deleted
 * @param selectedSkill The skill name on the bubble
 */
function deleteSkill(deleteButton, bubble, selectedSkill) {
    bubble.remove();
    selectedSkills = selectedSkills.filter(s => s !== selectedSkill);
    skillsSelect.value = "Add Skills";
    skillsSelect.style.color = "grey";
    const hiddenInput = document.getElementById("hidden" + selectedSkill);
    hiddenInput.remove();
}

/**
 * Listener to remake all skill bubble that were inputted previously if there was an error when the form was submitted.
 */
document.addEventListener("DOMContentLoaded", () => {
    const previousSkills = document.querySelectorAll(".previous-input");

    previousSkills.forEach(skill => {
        const selectedSkill = skill.dataset.skill;
        const displayName = skill.dataset.skilldisplayname;
        createSkillBubble(selectedSkill, displayName);
    })
})
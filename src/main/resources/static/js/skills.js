const skillsSelect= document.getElementById("skills-select");
const skillInputContainer= document.getElementById("skill-input-container");
const validSkills = Array.from(skillsSelect.options).map(skill => skill.value);

let selectedSkills = []

/**
 * A listener to add a skill to the list of selected skills if it hasn't already been added when an option is pressed on
 * the selection box. Calls the method to create the bubble when a skill is added.
 */
skillsSelect.addEventListener("change", () => {
    const selectedSkill = skillsSelect.value.trim();
    if (validSkills.includes(selectedSkill)) {
        if (!selectedSkills.includes(selectedSkill)) {
            selectedSkills.push(selectedSkill);
            createSkillBubble(selectedSkill);
        }
    }
    if (selectedSkill !== "Add Skills") {
        skillsSelect.style.color = "black";
    }
});

/**
 * Creates the skill bubble underneath the selection field with a close button. Removes the skill from the selected skills
 * list if the close button is pressed.
 * @param skill The skill name string
 */
function createSkillBubble(skill) {
    const bubble = document.createElement("span");
    bubble.className = "tag-box badge bg-success d-inline-flex align-items-center me-2 mb-2";

    const bubbleText = document.createElement("span");
    bubbleText.className = "tag-text-large ms-1 text-truncate";
    bubbleText.textContent = skill;
    bubble.appendChild(bubbleText);

    const deleteButton = document.createElement("button");
    deleteButton.className = "tag-delete-button-large btn-sm border-0 bg-transparent text-light ms-1";
    deleteButton.textContent = "X";
    deleteButton.addEventListener("click", () => {
        bubble.remove();
        selectedSkills = selectedSkills.filter(s => s !== skill);
        skillsSelect.value = "Add Skills";
        skillsSelect.style.color = "grey";
    });
    bubble.appendChild(deleteButton);

    skillInputContainer.appendChild(bubble);
}


/**
 * todo
 */
function addSkill() {
    const skillDropdown = document.getElementById("skills-select");
    const selectedSkill = skillDropdown.value;
    const selectedSkillName = skillDropdown.options[skillDropdown.selectedIndex].dataset.displayname;
    const selectedSkillsDiv = document.getElementById("selected-skills");

    const selectedSkillDisplay = document.createElement("div");
    const selectedSkillFeedback = document.createElement("div");
    const selectedSkillText = document.createElement("p");
    const selectedSkillInputHidden = document.createElement("input");
    const deleteButton = document.createElement("button");

    selectedSkillInputHidden.value = selectedSkill;
    selectedSkillInputHidden.type = "hidden"
    selectedSkillInputHidden.name = "skill";

    selectedSkillFeedback.className = "flex-grow-1 card d-flex pt-3 align-items-center"
    selectedSkillText.className = "text-secondary"
    selectedSkillText.textContent = selectedSkillName;

    selectedSkillDisplay.className = "input-group my-4";

    deleteButton.className = "btn btn-outline-danger custom-light-border ms-3"
    deleteButton.textContent = "❌"

    deleteButton.addEventListener("click", () => {
        selectedSkillDisplay.remove();
    });
    selectedSkillsDiv.appendChild(selectedSkillDisplay);
    selectedSkillFeedback.appendChild(selectedSkillText)
    selectedSkillDisplay.appendChild(selectedSkillFeedback);
    selectedSkillDisplay.appendChild(selectedSkillInputHidden);
    selectedSkillDisplay.appendChild(deleteButton);
    skillDropdown.selectedIndex = 0;
}
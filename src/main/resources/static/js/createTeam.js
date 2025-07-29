function addSkill() {
    const skillDropdown = document.getElementById("skills-select");
    const selectedSkill = skillDropdown.value;
    const selectedSkillName = skillDropdown.getAttribute("display-name");
    const selectedSkillDisplay = document.createElement("div");
    const selectedSkillInput = document.createElement("input");
    selectedSkillDisplay.appendChild(selectedSkillInput);
    selectedSkillInput.value = selectedSkill;
    selectedSkillInput.textContent = selectedSkillName;
    selectedSkillInput.readOnly = true;
    selectedSkillInput.className = "form-control";
    selectedSkillInput.name = "skill";
    selectedSkillDisplay.className = "input-group my-4";
    const deleteButton = document.createElement("button");
    deleteButton.className = "btn btn-outline-danger custom-light-border ms-3"
    deleteButton.textContent = "❌"
    selectedSkillDisplay.appendChild(deleteButton);
    deleteButton.addEventListener("click", () => {
        selectedSkillDisplay.remove();
    });
    const selectedSkillsDiv = document.getElementById("selected-skills");
    selectedSkillsDiv.appendChild(selectedSkillDisplay);
    skillDropdown.selectedIndex = 0;
}
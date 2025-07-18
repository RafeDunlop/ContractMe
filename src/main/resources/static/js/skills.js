const skillsInput = document.getElementById("skills-input");
const dataList     = document.getElementById("skills");
const skillInputContainer = document.getElementById("skill-input-container")
const validSkills = Array.from(dataList.options).map(skill => skill.value);

let selectedSkills = []

skillsInput.addEventListener("click", () => {
    dataList.focus();

    if (typeof skillsInput.showPicker === "function") {
        skillsInput.showPicker();
    }
})

skillsInput.addEventListener("input", function () {
    const selectedSkill = skillsInput.value.trim();
    if (validSkills.includes(selectedSkill) && !selectedSkills.includes(selectedSkill)) {
        selectedSkills.push(selectedSkill);
        selectedSkills.sort();
        skillsInput.value = "";
        createSkillBubble(selectedSkill);
    } else if (validSkills.includes(selectedSkill)) {
        skillsInput.value = "";
    }
})

function createSkillBubble (selectedSkill) {
    const bubble = document.createElement("span");
    bubble.className = "tag-box badge bg-success d-inline-flex align-items-center me-2 mb-2";

    const bubbleText = document.createElement("span");
    bubbleText.className = "tag-text-large text-truncate ms-1";
    bubbleText.textContent = selectedSkill;
    bubble.appendChild(bubbleText);

    const deleteButton = document.createElement("button")
    deleteButton.className = "tag-delete-button-large btn-sm border-0 bg-transparent text-light ms-1"
    deleteButton.textContent = "X";

    deleteButton.addEventListener("click", () => {
        skillInputContainer.removeChild(bubble);
        selectedSkills = selectedSkills.filter(s => s !== selectedSkill);
    });

    bubble.appendChild(deleteButton);
    skillInputContainer.appendChild(bubble);
}
const skillsSelect= document.getElementById("skills-select");
const skillInputContainer= document.getElementById("skill-input-container");
const validSkills = Array.from(skillsSelect.options).map(skill => skill.value);

let selectedSkills = []

skillsSelect.addEventListener("change", () => {
    const selectedSkill = skillsSelect.value.trim();
    if (validSkills.includes(selectedSkill)) {
        if (!selectedSkills.includes(selectedSkill)) {
            selectedSkills.push(selectedSkill);
            createSkillBubble(selectedSkill);
            console.log(selectedSkills);
        }
    }
});

// your existing bubble logic, unchanged:
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
        console.log(selectedSkills);
    });
    bubble.appendChild(deleteButton);

    skillInputContainer.appendChild(bubble);
}
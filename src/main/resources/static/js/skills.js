const skillsInput= document.getElementById("skills-input");
const skillInputContainer= document.getElementById("skill-input-container");
const dropdown= document.getElementById("skills-dropdown");
let selectedSkills= [];

skillInputContainer.addEventListener("click", () => {
    dropdown.style.display = "block";
    skillsInput.focus();
});

dropdown.addEventListener("click", e => {
    if (e.target.tagName !== "LI") return;
    const skill = e.target.textContent.trim();
    if (!selectedSkills.includes(skill)) {
        selectedSkills.push(skill);
        selectedSkills.sort();
        createSkillBubble(skill);
    }
    dropdown.style.display = "none";
    skillsInput.value = "";
});

document.addEventListener("click", e => {
    if (!skillInputContainer.contains(e.target)) {
        dropdown.style.display = "none";
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
    deleteButton.className = "btn-sm border-0 bg-transparent text-light ms-1";
    deleteButton.textContent = "X";
    deleteButton.addEventListener("click", () => {
        bubble.remove();
        selectedSkills = selectedSkills.filter(s => s !== skill);
    });
    bubble.appendChild(deleteButton);

    skillInputContainer.insertBefore(bubble, skillsInput);
}
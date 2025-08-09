/** Class for creating team requests on the createTeam template, accessed from viewRenovation template */

/**
 * Adds a skill field with a hidden input, display and delete button to a specified empty div
 * input value is enum value, e.g. MANUAL_LABOURER, named skill
 */
function addSkill() {
    const skillDropdown = document.getElementById("skills-select");
    if (skillDropdown.selectedIndex !== 0) {
        const selectedSkill = skillDropdown.value;
        document.getElementById("skills-list");
        const selectedSkillName = skillDropdown.options[skillDropdown.selectedIndex].dataset.displayname;
        const selectedSkillsDiv = document.getElementById("selected-skills");

        const selectedSkillDisplay = document.createElement("div");
        const selectedSkillFeedback = document.createElement("div");
        const selectedSkillText = document.createElement("p");
        const selectedSkillInputHidden = document.createElement("input");
        const deleteButton = document.createElement("button");

        selectedSkillInputHidden.value = selectedSkill;
        selectedSkillInputHidden.type = "hidden";
        selectedSkillInputHidden.name = "skills";

        selectedSkillFeedback.className = "d-flex fustify-content-between align-items-start";
        selectedSkillText.className = "w-100 text-secondary";
        selectedSkillText.textContent = selectedSkillName;

        selectedSkillDisplay.className = "list-group-item p-3 mb-3 shadow-sm rounded bg-white position-relative";

        deleteButton.className = "btn btn-outline-danger custom-light-border ms-3";
        deleteButton.textContent = "❌";

        deleteButton.addEventListener("click", () => {
            selectedSkillDisplay.remove();
            updateErrorMessageLabels()
        });
        selectedSkillsDiv.appendChild(selectedSkillDisplay);
        selectedSkillFeedback.appendChild(selectedSkillText);
        selectedSkillDisplay.appendChild(selectedSkillFeedback);
        selectedSkillDisplay.appendChild(selectedSkillInputHidden);
        selectedSkillFeedback.appendChild(deleteButton);
        skillDropdown.selectedIndex = 0;

        updateErrorMessageLabels()
    }
}

/**
 * Controls the enabled state of the "Add" button based on skill selection.
 */
document.getElementById('skills-select').addEventListener('change', function() {
    const addButton = document.getElementById('addSkillButton');
    addButton.disabled = this.selectedIndex === 0;
});

/** Used for front end validation for checking amount of skills selected */
function getSkillCount() {
    return document.querySelectorAll('#selected-skills input[name="skills"]').length;
}

/** Update error message labels*/
function updateErrorMessageLabels() {
    const count = getSkillCount();

    let sizeFrontendError = document.getElementById("skill-frontend-error");
    let sizeFrontendErrorMessage = document.getElementById("skill-frontend-error-message");

    if (count === 0) {
        sizeFrontendErrorMessage.textContent = "Your team request must have at least one role.";
        sizeFrontendError.hidden = false;
    } else if (count > 5) {
        sizeFrontendErrorMessage.textContent = "Your team request cannot have more than 5 roles.";
        sizeFrontendError.hidden = false;
    } else {
        sizeFrontendErrorMessage.textContent = "";
        sizeFrontendError.hidden = true;
    }
    return count;
}
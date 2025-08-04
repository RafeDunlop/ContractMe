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
        selectedSkillInputHidden.name = "skill";

        selectedSkillFeedback.className = "d-flex fustify-content-between align-items-start";
        selectedSkillText.className = "w-100 text-secondary";
        selectedSkillText.textContent = selectedSkillName;

        selectedSkillDisplay.className = "list-group-item p-3 mb-3 shadow-sm rounded bg-white position-relative";

        deleteButton.className = "btn btn-outline-danger custom-light-border ms-3";
        deleteButton.textContent = "❌";

        deleteButton.addEventListener("click", () => {
            selectedSkillDisplay.remove();
        });
        selectedSkillsDiv.appendChild(selectedSkillDisplay);
        selectedSkillFeedback.appendChild(selectedSkillText);
        selectedSkillDisplay.appendChild(selectedSkillFeedback);
        selectedSkillDisplay.appendChild(selectedSkillInputHidden);
        selectedSkillFeedback.appendChild(deleteButton);
        skillDropdown.selectedIndex = 0;
    }
}

/**
 * Creates a post request attaching the input to a TeamRequestDTO to the endpoint in the TeamsController.
 * Also creates TeamRoleDTO's within the DTO which has a null field for contractor and not accepted field for accepted.
 */
document.getElementById('create-team-form').addEventListener('submit', function(e) {
    e.preventDefault();
    const skillInputs = document.querySelectorAll('input[name="skill"]');
    const requestBody = {
        renovationRecordId: new URLSearchParams(window.location.search).get('id'),
        roles: Array.from(skillInputs).map(input => ({
            skill: input.value,
            contractorId: null,
            accepted: false
        }))
    };
    fetch('/renovations/team/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        },
        body: JSON.stringify(requestBody)
    }).then(response => {
        if (response.ok) {
            window.location.href = `/renovations/view?id=${new URLSearchParams(window.location.search).get('id')}`;
        }
    })
    .catch(error => {
        console.error('Error:', error);
    });
});

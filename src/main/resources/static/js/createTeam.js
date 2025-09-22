/**
 * Adds a skill field with a hidden input, display and delete button to a specified empty div
 * input value is enum value, e.g. MANUAL_LABOURER, named skill
 */
function addSkill() {
    const skillDropdown = document.getElementById("skills-select");
    if (skillDropdown.selectedIndex !== 0) {
        const selectedSkillName = skillDropdown.options[skillDropdown.selectedIndex].dataset.displayname;
        const selectedSkill = skillDropdown.value;
        const skillsGridRow = document.querySelector(".container.text-center .row");

        const existingSkillCards = skillsGridRow.querySelectorAll(".skill-card");
        if (existingSkillCards.length >= 5) {
            displayTooManyRolesError();
            skillDropdown.selectedIndex = 0;
            return;
        }

        const skillCol = document.createElement("div");
        skillCol.className = "col skill-card d-flex justify-content-center";

        const skillCardContainer = document.createElement("div");
        skillCardContainer.className = "skill-card-container card container border-5 border-dark-subtle align-items-center";
        skillCardContainer.style.paddingTop = "6vh";
        skillCardContainer.style.paddingBottom = "4vh";
        skillCardContainer.style.maxWidth = "14vw";
        skillCardContainer.style.height = "35vh";
        skillCardContainer.style.maxHeight = "400px";
        skillCardContainer.style.margin = "5vh 5% 4vh";
        skillCardContainer.style.position = "relative";

        // Image and text
        const skillImg = document.createElement("img");
        skillImg.src = "icons/profile-icon.svg";
        skillImg.alt = "default-icon";
        skillImg.style.width = "75%";
        skillImg.style.marginBottom = "0";
        skillImg.style.marginTop = "0";

        const skillTitle = document.createElement("h5");
        skillTitle.textContent = selectedSkillName;
        skillTitle.style.marginBottom = "1vh";
        skillTitle.className = "selected-skill-name";

        const nameTitle = document.createElement("h5");
        nameTitle.textContent = "Role:";
        nameTitle.style.marginBottom = "1vh";

        // Delete button
        const deleteSkillBtn = document.createElement("button");
        deleteSkillBtn.className = "delete-skill-btn btn btn-outline-danger custom-light-border ms-3";
        deleteSkillBtn.textContent = "❌";

        const hiddenInput = document.createElement("input");
        hiddenInput.type = "hidden";
        hiddenInput.name = "skills";
        hiddenInput.value = selectedSkill;

        skillCol.appendChild(hiddenInput);

        deleteSkillBtn.addEventListener("click", () => {
            skillCol.remove();
            updateErrorMessageLabels();
        });

        skillCardContainer.appendChild(deleteSkillBtn);
        skillCardContainer.appendChild(skillImg);
        skillCardContainer.appendChild(nameTitle);
        skillCardContainer.appendChild(skillTitle);

        skillCol.appendChild(skillCardContainer);
        skillsGridRow.appendChild(skillCol);
        skillDropdown.selectedIndex = 0;
        updateErrorMessageLabels();
    }
}

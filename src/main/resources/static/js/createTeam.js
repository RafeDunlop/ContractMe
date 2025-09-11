function updateAddRoleVisibility() {
    const skillsGridRow = document.querySelector(".container.text-center .row");
    const addRoleCard = document.getElementById("add-role-card");
    const skillCards = skillsGridRow.querySelectorAll(".skill-card");
    const count = skillCards.length;
    if (count >= 5) {
        if (addRoleCard) {
            addRoleCard.style.display = "none";
        }
    } else {
        if (addRoleCard) {
            addRoleCard.style.display = "";
        }
    }
}

function addSkill() {
    const skillDropdown = document.getElementById("skills-select");
    if (skillDropdown.selectedIndex !== 0) {
        const selectedSkill = skillDropdown.value;
        const selectedSkillName = skillDropdown.options[skillDropdown.selectedIndex].dataset.displayname;

        const selectedSkillsDiv = document.getElementById("selected-skills");
        const skillsGridRow = document.querySelector(".container.text-center .row");

        const existingSkillCards = skillsGridRow.querySelectorAll(".skill-card");
        if (existingSkillCards.length >= 5) {
            return;
        }

        const selectedSkillDisplay = document.createElement("div");
        const selectedSkillFeedback = document.createElement("div");
        const selectedSkillText = document.createElement("p");
        const selectedSkillInputHidden = document.createElement("input");
        const deleteButton = document.createElement("button");

        selectedSkillInputHidden.value = selectedSkill;
        selectedSkillInputHidden.type = "hidden";
        selectedSkillInputHidden.name = "skills";

        selectedSkillFeedback.className = "d-flex justify-content-between align-items-start";
        selectedSkillText.className = "w-100 text-secondary";
        selectedSkillText.textContent = selectedSkillName;

        selectedSkillDisplay.className = "list-group-item p-3 mb-3 shadow-sm rounded bg-white position-relative";

        deleteButton.className = "btn btn-outline-danger custom-light-border ms-3";
        deleteButton.textContent = "❌";

        let skillCard;

        deleteButton.addEventListener("click", () => {
            selectedSkillDisplay.remove();
            if (skillCard) {
                skillCard.remove();
            }
            updateErrorMessageLabels();
            updateAddRoleVisibility();
        });

        selectedSkillsDiv.appendChild(selectedSkillDisplay);
        selectedSkillFeedback.appendChild(selectedSkillText);
        selectedSkillDisplay.appendChild(selectedSkillFeedback);
        selectedSkillDisplay.appendChild(selectedSkillInputHidden);
        selectedSkillFeedback.appendChild(deleteButton);

        const skillCol = document.createElement("div");
        skillCol.className = "col skill-card";

        const skillCardContainer = document.createElement("div");
        skillCardContainer.className = "skill-card-container card container border-5 border-dark-subtle align-items-center";
        skillCardContainer.style.paddingTop = "7vh";
        skillCardContainer.style.paddingBottom = "4vh";
        skillCardContainer.style.maxWidth = "14vw";
        skillCardContainer.style.height = "35vh";
        skillCardContainer.style.margin = "5vh 5% 4vh";
        skillCardContainer.style.position = "relative";

// Image and text
        const skillImg = document.createElement("img");
        skillImg.src = "icons/profile-icon.svg";
        skillImg.alt = "default-icon";
        skillImg.style.width = "75%";
        skillImg.style.marginBottom = "1vh";
        skillImg.style.marginTop = "1vh";

        const skillTitle = document.createElement("h6");
        skillTitle.textContent = selectedSkillName;

// Delete button
        const deleteSkillBtn = document.createElement("div");
        deleteSkillBtn.className = "delete-skill-btn";
        deleteSkillBtn.textContent = "X";

        deleteSkillBtn.addEventListener("click", () => {
            skillCol.remove();
            selectedSkillDisplay.remove();
            updateErrorMessageLabels();
            updateAddRoleVisibility();
        });

// Append elements
        skillCardContainer.appendChild(deleteSkillBtn);
        skillCardContainer.appendChild(skillImg);
        skillCardContainer.appendChild(skillTitle);

        skillCol.appendChild(skillCardContainer);
        skillsGridRow.appendChild(skillCol);


        skillDropdown.selectedIndex = 0;

        updateAddRoleVisibility();
        updateErrorMessageLabels();
    }
}

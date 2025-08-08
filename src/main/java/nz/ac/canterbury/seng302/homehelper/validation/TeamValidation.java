package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class responsible for validating team creation requests.
 */
@Service
public class TeamValidation {

    /**
     * Validates that the team contains at least one role.
     * @param teamRequestDTO The DTO representing the creation request.
     * @return An error message if team is empty or too big, otherwise null if valid.
     */
    public String validateTeamSize(TeamRequestDTO teamRequestDTO) {
        List<String> skills = teamRequestDTO.getSkills();
        if (skills == null || skills.isEmpty()) {
            return "Your team request must have at least one role.";
        }
        if (skills.size() > 5) {
            return "Your team request cannot have more than 5 roles.";
        }
        return null;
    }

    /**
     * Validates that each role in the request contains a skill.
     * @param teamRequestDTO The DTO representing the creation request.
     * @return An error message if a role is missing a skill, null if valid.
     */
    public String validateSkills(TeamRequestDTO teamRequestDTO) {
        List<String> skills = teamRequestDTO.getSkills();

        if (skills == null) {
            return null;
        }

        for (String skillName : skills) {
            if (Skill.findEnumValueFromDisplayName(skillName) == null) return "Error: '" + skillName + "' is not a valid skill.";

            return null;
        }

        return null;
    }
}

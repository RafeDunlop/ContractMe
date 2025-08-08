package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.dto.TeamRoleDTO;
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
     * @return An error message if team is empty, otherwise null if valid.
     */
    public String validateNotEmpty(TeamRequestDTO teamRequestDTO) {
        if (teamRequestDTO.getRoles() == null || teamRequestDTO.getRoles().isEmpty()) {
            return "Team must contain at least one role";
        }
        return null;
    }

    /**
     * Validates that each role in the request contains a skill.
     * @param teamRequestDTO The DTO representing the creation request.
     * @return An error message if a role is missing a skill, null if valid.
     */
    public String validateAllRolesHaveSkill(TeamRequestDTO teamRequestDTO) {
        List<TeamRoleDTO> roles =  teamRequestDTO.getRoles();

        for (int i = 0; i < roles.size(); i++) {
            TeamRoleDTO teamRoleDTO = roles.get(i);
            if (teamRoleDTO.getSkill() == null) {
                return String.format("Role %d must have a skill selected.", i + 1);
            }
        }
        return null;
    }
}

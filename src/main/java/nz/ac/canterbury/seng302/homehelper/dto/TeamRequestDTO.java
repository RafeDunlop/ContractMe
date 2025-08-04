package nz.ac.canterbury.seng302.homehelper.dto;

import java.util.List;

/**
 * A Data Transfer Object (DTO) that represents a request for team-related information.
 * This class is designed to hold details required for team formation or modifications,
 * including a reference to a renovation record and a list of roles associated with the team.
 */
public class TeamRequestDTO {

    private Long renovationRecordId;
    private List <TeamRoleDTO> roles;

    public TeamRequestDTO(Long renovationRecordId, List<TeamRoleDTO> teamRoles) {
        this.roles = teamRoles;
    }

    public Long getRenovationRecordId() {
        return renovationRecordId;
    }

    public List<TeamRoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(List<TeamRoleDTO> roles) {
        this.roles = roles;
    }

}

package nz.ac.canterbury.seng302.homehelper.dto;

import java.util.List;

/**
 * A Data Transfer Object (DTO) that represents a request for team-related information.
 * This class is designed to hold details required for team formation or modifications,
 * including a reference to a renovation record and a list of roles associated with the team.
 */
public class TeamRequestDTO {
    public TeamRequestDTO() {}

    private List<String> skills;

    private boolean invitesAutomatic;

    public boolean isInvitesAutomatic() {
        return invitesAutomatic;
    }

    public void setInvitesAutomatic(boolean invitesAutomatic) {
        this.invitesAutomatic = invitesAutomatic;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}

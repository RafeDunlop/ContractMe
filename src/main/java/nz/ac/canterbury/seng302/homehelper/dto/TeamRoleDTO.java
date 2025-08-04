package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;

/**
 * A Data Transfer Object (DTO) for representing the role of a team member with relation
 * to a specific skill and contractor assignment in a project.
 *
 * This class encapsulates information about a team role, including the required skill,
 * the contractor associated with the role, and whether the role has been accepted.
 */
public class TeamRoleDTO {

    private Skill skill;
    private Long contractorId;
    private boolean accepted;

    public TeamRoleDTO(Skill skill) {
        this.skill = skill;
        this.accepted = false;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Long getContractorId() {
        return contractorId;
    }

    public void setContractorId(Long contractorId) {
        this.contractorId = contractorId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }


}

package nz.ac.canterbury.seng302.homehelper.entity.users;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

/**
 * Represents a role within the system. A role is associated with a contractor, a specific skill,
 * and an accepted status to indicate whether the role is confirmed or not.
 * This is an embeddable entity and is designed to be part of the team entity class.
 */
@Embeddable
public class Role {

    @ManyToOne
    private Contractor contractor;

    private Skill skill;

    private boolean accepted;

    public Role() {}

    public Role(Skill skill) {
        this.contractor = null;
        this.skill = skill;
        this.accepted = false;
    }

    public Role(Contractor contractor, Skill skill, boolean accepted) {
        this.contractor = contractor;
        this.skill = skill;
        this.accepted = accepted;
    }

    public Contractor getContractor() {
        return contractor;
    }

    public void setContractor(Contractor contractor) {
        this.contractor = contractor;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

}

package nz.ac.canterbury.seng302.homehelper.entity.users;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/**
 * Represents a role within the system. A role is associated with a contractor, a specific skill,
 * and an accepted status to indicate whether the role is confirmed or not.
 * This is an embeddable entity and is designed to be part of the team entity class.
 */
@Embeddable
public class Role {

    private Long contractorId;

    @Column
    private LocalDateTime creationDate;

    private Skill skill;

    @Column(nullable = false)
    private RoleStatus status;

    public Role() {}

    public Role(Skill skill) {
        this.contractorId = null;
        this.skill = skill;
        this.status = RoleStatus.UNFILLED;
        creationDate = LocalDateTime.now();
    }

    public Role(Contractor contractor, Skill skill, RoleStatus status) {
        this.contractorId = contractor.getId();
        this.skill = skill;
        this.status = status;
        creationDate = LocalDateTime.now();
    }

    public Long getContractorId() {
        return contractorId;
    }

    public void setContractor(Contractor contractor) {
        this.contractorId = (contractor == null) ? null : contractor.getId();
        if (this.status == RoleStatus.UNFILLED) setStatus(RoleStatus.WAITING);
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public RoleStatus getStatus() {
        return status;
    }

    public void setStatus(RoleStatus status) {
        if (status == RoleStatus.UNFILLED) assert this.contractorId == null;
        this.status = status;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
}

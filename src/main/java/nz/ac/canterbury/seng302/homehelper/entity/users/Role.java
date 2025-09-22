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

    /**
     * Default constructor required by JPA.
     */
    public Role() {}

    /**
     * Creates a new role with the given skill.
     * The contractor is initially unassigned, and the status is set to {@link RoleStatus#UNFILLED}.
     *
     * @param skill the skill associated with this role
     */
    public Role(Skill skill) {
        this.contractorId = null;
        this.skill = skill;
        this.status = RoleStatus.UNFILLED;
        creationDate = LocalDateTime.now();
    }

    /**
     * Creates a new role with the given contractor, skill, and status.
     *
     * @param contractor the contractor assigned to the role
     * @param skill the skill associated with the role
     * @param status the initial status of the role
     */
    public Role(Contractor contractor, Skill skill, RoleStatus status) {
        this.contractorId = contractor.getId();
        this.skill = skill;
        this.status = status;
        creationDate = LocalDateTime.now();
    }

    /**
     * Returns the ID of the contractor assigned to this role.
     *
     * @return the contractor ID, or {@code null} if no contractor is assigned
     */
    public Long getContractorId() {
        return contractorId;
    }

    /**
     * Assigns a contractor to this role.
     * If {@code contractor} is {@code null}, the contractor assignment is cleared.
     * If the current status is {@link RoleStatus#UNFILLED}, it is automatically changed to {@link RoleStatus#WAITING}.
     *
     * @param contractor the contractor to assign, or {@code null} to remove the assignment
     */
    public void setContractor(Contractor contractor) {
        this.contractorId = (contractor == null) ? null : contractor.getId();
        if (this.status == RoleStatus.UNFILLED) setStatus(RoleStatus.WAITING);
    }

    /**
     * Returns the skill associated with this role.
     *
     * @return the skill
     */
    public Skill getSkill() {
        return skill;
    }

    /**
     * Sets the skill for this role.
     *
     * @param skill the new skill
     */
    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    /**
     * Returns the current status of this role.
     *
     * @return the role status
     */
    public RoleStatus getStatus() {
        return status;
    }

    /**
     * Updates the status of this role.
     * If the status is set to {@link RoleStatus#UNFILLED}, this method enforces that
     * no contractor is assigned (i.e., {@code contractorId == null}).
     *
     * @param status the new role status
     * @throws AssertionError if setting to UNFILLED while a contractor is still assigned
     */
    public void setStatus(RoleStatus status) {
        assert status != RoleStatus.UNFILLED || this.contractorId == null;
        this.status = status;
    }

    /**
     * Removes the contractor from this role without modifying the current status.
     * Use with caution: the role may remain in a non-UNFILLED status even after the contractor is removed.
     */
    public void removeContractor() {
        this.contractorId = null;
    }

    /**
     * Returns the creation date of this role.
     *
     * @return the creation date
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creation date of this role.
     *
     * @param creationDate the new creation date
     */
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
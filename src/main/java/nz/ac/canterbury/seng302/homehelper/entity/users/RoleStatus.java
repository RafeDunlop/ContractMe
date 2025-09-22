package nz.ac.canterbury.seng302.homehelper.entity.users;

/**
 * Represents the status of a role within the system.
 * A role may be unfilled, awaiting contractor confirmation,
 * or accepted by the assigned contractor.
 */
public enum RoleStatus {

    /**
     * No contractor is assigned to the role, and the position is unfilled.
     */
    UNFILLED,

    /**
     * A contractor has been invited but has not yet responded.
     */
    WAITING,

    /**
     * The contractor has accepted the invitation and the role is confirmed.
     */
    ACCEPTED
}

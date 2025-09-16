package nz.ac.canterbury.seng302.homehelper.entity.users;

public enum RoleStatus {
    UNFILLED,       // no contractor assigned, unable to fill position
    WAITING,        // contractor invited, no response yet
    ACCEPTED,       // contractor accepted the invite
    DECLINED        // contractor declined the invite, rerun algorithm
}

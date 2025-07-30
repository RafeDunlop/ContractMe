package nz.ac.canterbury.seng302.homehelper.entity;


/**
 * Represents the various possible states that a task can be in.
 * Each state has a human-readable display string associated with it.
 */
public enum TaskState {

    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),

    BLOCKED("Blocked"),

    COMPLETED("Completed"),

    CANCELLED("Cancelled");

    private String displayState;

    /**
     * Constructs a new TaskState with the specified display string.
     *
     * @param displayState the human-readable name for this task state
     */
    TaskState(String displayState) {
        this.displayState = displayState;
    }

    /**
     * Returns the human-readable display name of this task state.
     *
     * @return the display string for the task state
     */
    public String getDisplayState() {
        return displayState;
    }


}

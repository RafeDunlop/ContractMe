package nz.ac.canterbury.seng302.homehelper.entity;


/**
 * Represents the various possible states that a task can be in.
 * Each state has a human-readable display string associated with it.
 */
public enum TaskState {

    NOT_STARTED("Not Started", "#c2cad1"),
    IN_PROGRESS("In Progress", "#1982C4"),

    BLOCKED("Blocked", "#FFCA3A"),

    COMPLETED("Completed", "#8AC926"),

    CANCELLED("Cancelled", "#FF595E");

    private final String displayState;
    private final String colorHex;
    /**
     * Constructs a new TaskState with the specified display string.
     *
     * @param displayState the human-readable name for this task state
     */
    TaskState(String displayState, String colorHex) {
        this.displayState = displayState;
        this.colorHex = colorHex;
    }

    /**
     * Get a TaskState instance from its lowerCamelCase name.
     * @param camelCaseName the camelCaseName used by the javascript input
     * @return the TaskState instance
     * @throws IllegalArgumentException if the argument is unknown
     */
    public static TaskState fromCamelCaseName(String camelCaseName) throws IllegalArgumentException {
        return switch (camelCaseName) {
            case "notStarted" -> NOT_STARTED;
            case "inProgress" -> IN_PROGRESS;
            case "blocked" -> BLOCKED;
            case "completed" -> COMPLETED;
            case "cancelled" -> CANCELLED;
            default -> throw new IllegalArgumentException("Unknown camel case name: " + camelCaseName);
        };
    }

    /**
     * Returns the human-readable display name of this task state.
     *
     * @return the display string for the task state
     */
    public String getDisplayState() {
        return displayState;
    }


    public String getColorHex() {
        return colorHex;
    }
}

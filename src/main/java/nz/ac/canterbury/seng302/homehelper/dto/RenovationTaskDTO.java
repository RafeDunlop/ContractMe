package nz.ac.canterbury.seng302.homehelper.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Creates a RenovationTaskDTO object
 */
public class RenovationTaskDTO {
    private String name;
    String description;
    LocalDate dueDate;

    /**
    Returns the description
     */
    public String getDescription() {
        return description;
    }
    /**
    Returns the name
     */
    public String getName() {
        return name;
    }

    /**
    Sets the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
    Sets the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
    Gets the due date
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
    Sets the due date
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
    Constructor
     */
    public RenovationTaskDTO(String name, String description, LocalDate dueDate) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
    }
}

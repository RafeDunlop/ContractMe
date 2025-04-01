package nz.ac.canterbury.seng302.homehelper.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Creates a RenovationTaskDTO object
 */
public class RenovationTaskDTO {
    private String name;
    String description;
    LocalDate dueDate;
    List<String> rooms;

    /**
     * constructor which accepts an initial state for an unvalidated and unsaved {@link nz.ac.canterbury.seng302.homehelper.entity.RenovationTask}
     * @param name The initial name
     * @param description The initial description
     * @param dueDate The initial due date
     * @param rooms the initial subset of rooms
     */
    public RenovationTaskDTO(String name, String description, LocalDate dueDate,List<String> rooms) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.rooms = rooms;
    }

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
     * gets the subset of rooms pertaining to the task
     * @return The subset of rooms pertaining to the task
     */
    public List<String> getRooms() {
        return rooms;
    }

    /**
     * Sets the description of this renovation task
     * @param description The description of this renovation task
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
     * Sets the rooms to be added to this DTO
     * @param rooms The rooms to be added to this DTO
     */
   public void setRooms(List<String> rooms) {
        this.rooms = rooms;
   }

}

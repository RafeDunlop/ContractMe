package nz.ac.canterbury.seng302.homehelper.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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

    public List<String> getRooms() {
        return rooms;
    }

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


    public RenovationTaskDTO(String name, String description, LocalDate dueDate,List<String> rooms) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.rooms = rooms;
    }
}

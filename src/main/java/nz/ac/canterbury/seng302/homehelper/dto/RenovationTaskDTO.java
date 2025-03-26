package nz.ac.canterbury.seng302.homehelper.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RenovationTaskDTO {
    private String name;
    String description;
    LocalDate dueDate;
    ArrayList<String> rooms;


    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getRooms() {
        return rooms;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public RenovationTaskDTO(String name, String description, LocalDate dueDate,ArrayList<String> rooms) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.rooms = rooms;
    }
}

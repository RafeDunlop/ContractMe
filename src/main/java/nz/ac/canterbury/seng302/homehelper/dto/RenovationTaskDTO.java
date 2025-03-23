package nz.ac.canterbury.seng302.homehelper.dto;

import java.time.LocalDateTime;

public class RenovationTaskDTO {
    private String name;
    String description;
    LocalDateTime dueDate;

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public RenovationTaskDTO(String name, String description, LocalDateTime dueDate) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
    }
}

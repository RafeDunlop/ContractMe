package nz.ac.canterbury.seng302.homehelper.dto;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.format.DateTimeFormatter;
/**
 * Creates a RenovationTaskDTO object
 */
public class RenovationTaskDTO {
    private Long id;
    private String name;
    private String description;
    String dueDate;
    private List<String> rooms;
    private String iconFileName;

    /**
     * Default constructor for {@code RenovationTaskDTO}.
     * Initializes the task with an empty name and description, a {@code null} due date,
     * and an empty list of rooms.
     */
    public RenovationTaskDTO() {
        this.id = null;
        this.name = "";
        this.description = "";
        this.dueDate = null;
        this.rooms = new ArrayList<>();
        this.iconFileName = "";
    }

    /**
     * constructor which accepts an initial state with an iconFileName and id {@link nz.ac.canterbury.seng302.homehelper.entity.RenovationTask}
     * @param id The tasks id
     * @param name The initial name
     * @param description The initial description
     * @param dueDate The initial due date
     * @param rooms the initial subset of rooms
     * @param iconFileName the name of the icon file
     */
    public RenovationTaskDTO(Long id, String name, String description, String dueDate,List<String> rooms, String iconFileName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.rooms = rooms;
        this.iconFileName = iconFileName;
    }

    /**
     * constructor which accepts an initial state for an unvalidated and unsaved {@link nz.ac.canterbury.seng302.homehelper.entity.RenovationTask}
     * @param name The initial name
     * @param description The initial description
     * @param dueDate The initial due date
     * @param rooms the initial subset of rooms
     */
    public RenovationTaskDTO(String name, String description, String dueDate,List<String> rooms) {
        this.name = name;
        this.description = description;
        this.dueDate = dueDate;
        this.rooms = rooms;
    }

    /**
     * Constructs a {@code RenovationTaskDTO} from an existing {@link RenovationTask} entity.
     * Copies the task name, description, due date, and list of rooms from the entity.
     * @param task the {@code RenovationTask} to convert into a DTO
     */
    public RenovationTaskDTO(RenovationTask task) {
        this.id = task.getId();
        this.name = task.getName();
        this.description = task.getDescription();
        if (task.getDueDate() != null) {
            this.dueDate = task.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            this.dueDate = "";
        }
        this.rooms = task.getRoomList();
        this.iconFileName = task.getIconFileName();
    }

    /**
     Returns the id
     */
    public Long getId() {return id;}
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
    public String getDueDate() {
        return dueDate;
    }

    /**
    Sets the due date
     */
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
   }

    /**
     * Sets the rooms to be added to this DTO
     * @param rooms The rooms to be added to this DTO
     */
   public void setRooms(List<String> rooms) {
        this.rooms = rooms;
   }

    public void setIconFileName(String iconFileName) {this.iconFileName = iconFileName;}

    public String getIconFileName() {return iconFileName;}
}

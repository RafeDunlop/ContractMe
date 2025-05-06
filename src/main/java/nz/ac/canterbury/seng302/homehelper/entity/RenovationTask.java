package nz.ac.canterbury.seng302.homehelper.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;

@Entity
public class RenovationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long task_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "renovation_id", nullable = false)
    private RenovationRecord renovationRecord;

    @Column(nullable=false)
    private String name;

    @Lob
    @Column(nullable=false)
    private String description;

    @ElementCollection
    private List<String> roomList;

    @Column
    private LocalDate dueDate;

    @Column
    private String iconFileName;


    /**
     * JPA required no-args constructor
     */
    public RenovationTask() {}

    /**
     * Constructor for RenovationTask object
     * @param name The name of teh renovation task
     * @param description the description of the renovation task
     * @param roomList The rooms associated with this task
     * @param dueDate The due date of this task
     */
    public RenovationTask(String name, String description, List<String> roomList, LocalDate dueDate, RenovationRecord renovationRecord) {
        this.renovationRecord = renovationRecord;
        this.name = name.trim();
        this.description = (description != null) ? description.trim() : "";
        this.roomList = roomList;
        this.dueDate = dueDate;
        this.iconFileName = "default-icon.png";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<String> roomList) {
        this.roomList = roomList;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Long getId() {return task_id;}

    public void setIconFileName(String iconFileName) {
        this.iconFileName = iconFileName;
    }

    public String getIconFileName() {
        return iconFileName;
    }

    public RenovationRecord getRenovationRecord() { return renovationRecord; }
}

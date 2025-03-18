package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;
import nz.ac.canterbury.seng302.homehelper.entity.auxiliary.RenovationTaskId;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class RenovationTask {

    @EmbeddedId
    private RenovationTaskId renovationTaskId;

    @ManyToOne
    @MapsId("id")
    @JoinColumn(name = "id", nullable=false)
    private RenovationRecord renovationRecord;

    @Column(nullable=false)
    private String name;

    @Lob
    @Column(nullable=false)
    private String description;

    @ElementCollection
    private List<String> roomList;

    @Column
    private LocalDateTime dueDate;


    /**
     * JPA required no-args constructor
     */
    public RenovationTask() {}

    /**
     * Constructor for RenovationTask object
     * @param name
     * @param description
     * @param roomList
     * @param dueDate
     */
    public RenovationTask(String name, String description, List<String> roomList, LocalDateTime dueDate) {
        this.name = name.trim();
        this.description = (description != null) ? description.trim() : "";
        this.roomList = roomList;
        this.dueDate = dueDate;
    }
}

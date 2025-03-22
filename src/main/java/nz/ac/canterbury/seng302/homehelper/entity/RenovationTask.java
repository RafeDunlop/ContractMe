package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class RenovationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long task_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renovationId", referencedColumnName = "id", nullable = false)
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
    public RenovationTask(String name, String description, List<String> roomList, LocalDateTime dueDate, RenovationRecord renovationRecord) {
        this.renovationRecord = renovationRecord;
        this.name = name.trim();
        this.description = (description != null) ? description.trim() : "";
        this.roomList = roomList;
        this.dueDate = dueDate;
    }
}

package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class RenovationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "renovationRecordId", nullable=false)
    private RenovationRecord renovationRecord;

    @Column(nullable=false)
    private String name;

    @Lob
    @Column(nullable=false)
    private String description;

    @ElementCollection
    private List<String> rooms;

    @Column
    private LocalDateTime dueDate;


    /**
     * JPA required no-args constructor
     */
    public RenovationTask() {}
}

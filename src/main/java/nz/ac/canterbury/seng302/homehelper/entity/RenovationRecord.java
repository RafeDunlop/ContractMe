package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity which represents a renovation record
 * @author Jake Connolly
 */
@Entity
public class RenovationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(nullable = false)
    private String description;

    @ElementCollection
    private List<String> rooms;

    @Column
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime editedDate;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name="task_id")
    private List<RenovationTask> renovationTasks = new ArrayList<>();


    protected RenovationRecord() {}

    /**
     * Constructor for RenovationRecord
     * @param user The owner of this record
     * @param name of the record, unique
     * @param description of the record, not required
     * @param rooms a list of rooms for the renovation, not required
     */
    public RenovationRecord(User user, String name, String description, List<String> rooms) {
        this.user = user;
        this.name = name.trim(); //should not be possible to call constructor with empty string
        this.description = (description != null) ? description.trim() : "";
        this.rooms = new ArrayList<>();
        rooms.forEach(room -> this.rooms.add(room.trim()));
    }

    /**
     * Gets name of the renovation record
     * @return name of record
     */
    public String getName() {
        return name;
    }

    /**
     * Gets user of the renovation record
     * @return user of record
     */
    public User getUser(){
        return user;
    }

    /**
     * Gets id of the renovation record
     * @return id of record
     */
    public Long getId(){
        return id;
    }

    /**
     * Gets description of the renovation record
     * @return description of record
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets room list of the renovation record
     * @return rooms list of record
     */
    public List<String> getRooms() {
        return rooms;
    }

    /**
     * Gets created time of the renovation record
     * @return created time of record
     */
    public LocalDateTime getCreatedTimestamp() {
        return createdDate;
    }

    /**
     * Gets the list of all tasks made under a record
     * @return list of renovation tasks
     */
    public List<RenovationTask> getRenovationTasks() {return renovationTasks; }
    /**
     * Gets created date of the renovation record
     */
    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdDate = createdTimestamp;
    }

    /**
     * Sets the name of the renovation record
     * @param name of the record
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the user of the renovation record
     * @param user of the record
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Sets the description of the renovation record
     * @param description of the record
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the room list of the renovation record
     * @param rooms list of the record
     */
    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }

    /**
     * toString method returning all the values stored
     * @return string of the values of the record
     */
    @Override
    public String toString() {
        return "FormResult{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                "rooms=" + rooms +
                "createdDate=" + createdDate +
                ", editedDate='" + editedDate + '\'' +
                '}';
    }
}

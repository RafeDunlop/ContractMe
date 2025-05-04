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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "renovation_tags",
            joinColumns = @JoinColumn(name = "renovation_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 513)
    private String description;

    @ElementCollection
    private List<String> rooms;

    @Column
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime editedDate;

    @OneToMany(mappedBy = "renovationRecord",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<RenovationTask> renovationTasks = new ArrayList<>();

    @Column(nullable = false)
    private boolean isPublic = false;

    public RenovationRecord() {}

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
        this.tags = new ArrayList<>();
        rooms.forEach(room -> this.rooms.add(room.trim()));
    }

    /**
     * Sets task list, for testing
     * @param renovationTasks the list of tasks on the renovation record
     */
    public void setRenovationTasks(List<RenovationTask> renovationTasks) {
        this.renovationTasks = renovationTasks;
    }

    /**
     * Gets name of the renovation record
     * @return name of record
     */
    public String getName() {
        return name;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
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
     * Links the specified {@link Tag} to this entity
     * @param tag The {@link Tag} entity to be added to this entity's tags
     * @return The result of the {@link List} add operation; whether it was successful
     */
    public boolean addTag(Tag tag) {
        return tags.add(tag);
    }

    /**
     * Removes the specified {@link Tag} from this entity, if present
     * @param tag The {@link Tag} entity to be removed from this entity's tags
     * @return The result of the {@link List} remove operation; whether it successfully removed teh {@link Tag}
     */
    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }
    /**
     * Sets the publicity status of the renovation record
     * @param status of the publicity of renovation record
     */
    public void setPublicity(boolean status) {
        this.isPublic = status;
    }
    /**
     * Gets the publicity status of the renovation record
     * @return publicity status of renovation record
     */
    public boolean isPublic() {
        return isPublic;
    }
    /**
     * toString method returning all the values stored
     * @return string of the values of the record
     */
    @Override
    public String toString() {
        return "RenovationRecord{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                "rooms=" + rooms +
                "createdDate=" + createdDate +
                ", editedDate='" + editedDate + '\'' +
                '}';
    }
}

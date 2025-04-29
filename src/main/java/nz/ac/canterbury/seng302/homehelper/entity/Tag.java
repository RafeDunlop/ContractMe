package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

/**
 * Entity which represents a tag associated with 0 or more {@link RenovationRecord} entities
 * @author Rafe Dunlop
 */
@Entity
public class Tag {

    @Id
    @Column(name = "tag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tagName;

    /**
     * JPA required no-args constructor
     */
    public Tag() {}

    /**
     * Basic constructor for {@link Tag} entity
     * @param tagName The name of the tag to be created, must be unique
     */
    public Tag(String tagName) {
        this.tagName = tagName;
    }

    /**
     * Gets the value of this tag, it's name
     * @return The value of this tag
     */
    public String get() {
        return tagName;
    }

    /**
     * Gets the unique id of this entity
     * @return The id of this entity
     */
    public Long getId()  {
        return id;
    }

    /**
     * toString method returning all the values stored in this entity
     * @return string of the values of this tag
     */
    @Override
    public String toString() {
        return String.format("RenovationRecord{id=%d, value=%s}", id, tagName);
    }
}

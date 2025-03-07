package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entity class reflecting an entry of name, and favourite programming language
 * Note the @link{Entity} annotation required for declaring this as a persistence entity
 */
@Entity
public class FormResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String language;

    @Column
    private LocalDateTime createdTimestamp;

    /**
     * JPA required no-args constructor
     */
    protected FormResult() {}

    /**
     * Creates a new FormResult object
     * @param name name of user
     * @param language user's favourite programming language
     */
    public FormResult(String name, String language) {
        this.name = name;
        this.language = language;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public LocalDateTime getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    @Override
    public String toString() {
        return "FormResult{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", language='" + language + '\'' +
                ", createdTimestamp=" + createdTimestamp +
                '}';
    }
}

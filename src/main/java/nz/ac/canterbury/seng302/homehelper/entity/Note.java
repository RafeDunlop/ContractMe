package nz.ac.canterbury.seng302.homehelper.entity;

import jakarta.persistence.*;

@Entity
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // todo: add other properties

    public Note(String title, String content) {
        // todo: create note
    }

    /**
     * JPA required no-args constructor
     */
    public Note() {}

    public Long getId() {
        return id;
    }

}

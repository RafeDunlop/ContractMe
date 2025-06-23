package nz.ac.canterbury.seng302.homehelper.dto;

/**
 * A Data Transfer Object (DTO) for representing tag information
 * in a simplified and serializable format.
 */
public class TagDTO {
    private Long id;
    private String tagName;

    public TagDTO() {}

    public TagDTO(Long id, String tagName) {
        this.id = id;
        this.tagName = tagName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}

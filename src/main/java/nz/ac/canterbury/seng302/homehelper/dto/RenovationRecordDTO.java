package nz.ac.canterbury.seng302.homehelper.dto;

import java.util.List;

public class RenovationRecordDTO {
    private Long id;
    private String name;
    private String description;
    private boolean isPublic;
    private String createdTimestamp;
    private List<TagDTO> sortedTags;
    private Long userId;

    public RenovationRecordDTO(Long id, String name, String description, boolean isPublic,
                               String createdTimestamp, List<TagDTO> sortedTags, Long userId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.createdTimestamp = createdTimestamp;
        this.sortedTags = sortedTags;
        this.userId = userId;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getCreatedTimestamp() {
        return createdTimestamp;
    }

    public List<TagDTO> getSortedTags() {
        return sortedTags;
    }

    public Long getUserId() {
        return userId;
    }
}

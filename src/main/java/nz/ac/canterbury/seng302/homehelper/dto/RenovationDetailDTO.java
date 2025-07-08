package nz.ac.canterbury.seng302.homehelper.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class RenovationDetailDTO {
    private Long id;
    private String name;
    private String description;
    private boolean isPublic;
    private List<String> sortedTags;
    private List<String> rooms;
    private boolean isOwner;
    private Page<RenovationTaskDTO> paginatedTasks;

    public RenovationDetailDTO(Long id, String name, String description, boolean isPublic,
                               List<String> sortedTags, List<String> rooms,
                               boolean isOwner, Page<RenovationTaskDTO> paginatedTasks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.sortedTags = sortedTags;
        this.rooms = rooms;
        this.isOwner = isOwner;
        this.paginatedTasks = paginatedTasks;
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

    public List<String> getSortedTags() {
        return sortedTags;
    }

    public List<String> getRooms() {
        return rooms;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public Page<RenovationTaskDTO> getPaginatedTasks() {
        return paginatedTasks;
    }
}

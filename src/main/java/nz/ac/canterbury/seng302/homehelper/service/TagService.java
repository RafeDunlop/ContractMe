package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Access point for domain functionality which concerns renovation record tag entities, {@link Tag}
 * @author Rafe Dunlop
 */
@Service
public class TagService {

    private final TagRepository tagRepository;

    private final RenovationRecordRepository renovationRecordRepository;

    /**
     * Constructs repositories and validation classes as required.
     * @param tagRepository The repository for storing/updating/accessing {@link Tag} entities
     * @param renovationRecordRepository The repository for adding/removing tags from {@link RenovationRecord} entities
     */
    @Autowired
    public TagService(TagRepository tagRepository, RenovationRecordRepository renovationRecordRepository) {
        this.tagRepository = tagRepository;
        this.renovationRecordRepository = renovationRecordRepository;
    }

    public List<String> autocompleteTags(String tagName) {
        List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(tagName);
        return tags.stream().map(Tag::get).collect(Collectors.toList());
    }

    public void addTag(String name) {
        Tag tag = new Tag(name);
        tagRepository.save(tag);
    }
}

package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.validation.TagValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Access point for domain functionality which concerns renovation record tag entities, {@link Tag}
 * @author Rafe Dunlop
 */
@Service
public class TagService {

    private final TagRepository tagRepository;

    private final RenovationRecordRepository renovationRecordRepository;
    private final TagValidation tagValidation;
    /**
     * Constructs repositories and validation classes as required.
     * @param tagRepository The repository for storing/updating/accessing {@link Tag} entities
     * @param renovationRecordRepository The repository for adding/removing tags from {@link RenovationRecord} entities
     */
    @Autowired
    public TagService(TagRepository tagRepository, RenovationRecordRepository renovationRecordRepository, TagValidation tagValidation) {
        this.tagRepository = tagRepository;
        this.renovationRecordRepository = renovationRecordRepository;
        this.tagValidation = tagValidation;
    }

    /**
     * Returns a list of tag names like the given input by querying the repository
     * @param tagName the input to search for
     * @return a list of matching tag names
     */
    public List<String> autocompleteTags(String tagName) {
        List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(tagName);
        return tags.stream().map(Tag::get).collect(Collectors.toList());
    }

    public boolean checkExists(String tagName) {
        return tagRepository.findExactMatchTagByTagName(tagName).isEmpty();
    }


    public List<String> validateTag(RenovationRecord renovationRecord, String tagName) {
        List<String> errors = new ArrayList<>();
        if (renovationRecord.getTags().size() >= 5) {
            errors.add("Renovation cannot have more than 5 tags.");
        }
        if (renovationRecord.getTags().stream().map(Tag::getTagName).toList().contains(tagName.toLowerCase())) {
            errors.add("Renovation cannot contain duplicate tag names.");
        }
        errors.addAll(tagValidation.validateName(tagName));

        return errors;
    }

    public void addTagToRenovation(RenovationRecord record, String tagName) {
        record.getTags().add(getTag(tagName.toLowerCase().trim()));
        renovationRecordRepository.save(record);
    }

    /**
     * Creates and saves a new tag with a given name
     * @param name of the tag to be saved
     */
    public void addTag(String name) {
        Tag tag = new Tag(name.toLowerCase().trim());
        tagRepository.save(tag);
    }


    public Tag getTag(String tagName) {
        return tagRepository.findExactMatchTagByTagName(tagName).orElse(null);
    }
}

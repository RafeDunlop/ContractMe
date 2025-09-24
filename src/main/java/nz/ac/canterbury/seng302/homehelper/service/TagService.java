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
     * @param tagRepository The repository for storing/updating/accessing {@link Tag} entities.
     * @param renovationRecordRepository The repository for adding/removing tags from {@link RenovationRecord} entities.
     * @param tagValidation The validation class for tags.
     */
    @Autowired
    public TagService(TagRepository tagRepository, RenovationRecordRepository renovationRecordRepository, TagValidation tagValidation) {
        this.tagRepository = tagRepository;
        this.renovationRecordRepository = renovationRecordRepository;
        this.tagValidation = tagValidation;
    }

    /**
     * Returns a list of tag names like the given input by querying the repository.
     * @param tagName the input to search for.
     * @return a list of matching tag names.
     */
    public List<String> autocompleteTags(String tagName) {
        List<Tag> tags = tagRepository.findByNameContainingIgnoreCase(tagName);
        return tags.stream().map(Tag::get).collect(Collectors.toList());
    }

    /**
     * Checks if a tag with the name already exists in the database.
     * @param tagName the name to check if it already exists.
     * @return {@code true} if the tag does not exist, {@code false} otherwise.
     */
    public boolean checkExists(String tagName) {
        return tagRepository.findExactMatchTagByTagName(tagName.toLowerCase().trim()).isEmpty();
    }

    /**
     * Validates a tag name, with context of a renovation record.
     * Checks for a maximum of 5 tags, duplicate tag names.
     * @param renovationRecord The renovation record to validate against.
     * @param tagName The tag name to validate.
     * @return A list of validation error messages.
     */
    public List<String> validateTagAndRecord(RenovationRecord renovationRecord, String tagName) {
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

    /**
     * Adds a tag to the given renovation record.
     * @param record The renovation record to add the tag to.
     * @param tagName The name of the tag to add.
     */
    public void addTagToRenovation(RenovationRecord record, String tagName) {
        record.getTags().add(getTag(tagName.toLowerCase().trim()));
        renovationRecordRepository.save(record);
    }

    /**
     * Creates and saves a new tag with a given name.
     * @param name of the tag to be saved.
     */
    public void createTag(String name) {
        Tag tag = new Tag(name.toLowerCase().trim());
        tagRepository.save(tag);
    }

    /**
     * Getter for tag from repository.
     * @param tagName to get.
     * @return the desired tag.
     */
    public Tag getTag(String tagName) {
        return tagRepository.findExactMatchTagByTagName(tagName).orElse(null);
    }

    /**
     * Getter for tags from repository.
     * @param tagNames to get.
     * @return the desired tags from the repository.
     */
    public List<Tag> getTags(List<String> tagNames) {
        return tagRepository.findExactMatchTagsByTagNames(tagNames);
    }

    /**
     * Removes the specified tag from the {@link RenovationRecord} if it is there
     * @param record The renovation record to remove the tag from
     * @param tag The tag to be removed
     */
    public void removeTagFromRenovation(RenovationRecord record, Tag tag) {
        record.getTags().remove(tag);
        renovationRecordRepository.save(record);
    }
}

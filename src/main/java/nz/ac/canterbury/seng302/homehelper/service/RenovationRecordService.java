package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationRecordDTO;
import nz.ac.canterbury.seng302.homehelper.dto.TagDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.util.MapUtil;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationRecordValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Performs all logic to do with renovation records which does not face the UI
 * and interfaces with the repository
 * @author Jake Connolly
 */
@Service
public class RenovationRecordService {

    private final RenovationRecordRepository renovationRecordRepository;
    private final RenovationTaskRepository renovationTaskRepository;
    private final RenovationRecordValidation renovationRecordValidation;

    /**
     * Constructor for the RenovationRecordService class
     * @param renovationRecordRepository initializes with the repository for storing records
     */
    @Autowired
    public RenovationRecordService(RenovationRecordRepository renovationRecordRepository, RenovationTaskRepository renovationTaskRepository, RenovationRecordValidation renovationRecordValidation) {
        this.renovationRecordRepository = renovationRecordRepository;
        this.renovationTaskRepository = renovationTaskRepository;
        this.renovationRecordValidation = renovationRecordValidation;
    }

    /**
     * Creates a location and attaches it to the user entity
     * Saves the user with its location to the database
     *
     * @param renovation The renovation to attach location to
     * @param addressDTO Data transfer object for user registration
     *
     */
    public void addRenovationLocation(RenovationRecord renovation, AddressDTO addressDTO) {
        Location userLocation = new Location(
                addressDTO.getAddress_line1(),
                addressDTO.getCountry(),
                addressDTO.getPostcode(),
                addressDTO.getCity(),
                addressDTO.getRegion()
        );
        renovation.setLocation(userLocation);
        renovationRecordRepository.save(renovation);
    }

    /**
     * Retrieves a list of renovation records associated with the current user that are like the given term
     * with pagination. The results are sorted by relevance (based on name, description, and tags)
     * and then by creation time in descending order.
     *
     * @param user     The current user
     * @param term     The term to search for, not case-sensitive
     * @param pageable The pagination information
     * @return a list of renovation records from the user that match the term if given or tags
     */
    public Page<RenovationRecordDTO> getPaginatedUserRecords(User user, String term, List<Tag> tagList, Pageable pageable) {
        Page<RenovationRecord> rawPage;

        if (term.trim().isEmpty() && tagList == null) {
            rawPage = renovationRecordRepository.findUserRecords(user, Pageable.unpaged());
        } else if (tagList == null) {
            rawPage = renovationRecordRepository.findUserRecordsBySearch(user, term, Pageable.unpaged());
        } else if (term.trim().isEmpty()) {
            rawPage = renovationRecordRepository.findUserRecordsByTag(user, tagList, Pageable.unpaged());
        } else {
            rawPage = renovationRecordRepository.findUserRecordsBySearchOrTag(user, term, tagList, Pageable.unpaged());
        }

        List<RenovationRecord> sorted = rawPage.getContent().stream()
                .sorted(relevanceComparator(term, tagList))
                .toList();

        return toPage(sorted, pageable);
    }

    /**
     * Retrieves a list of public renovation records that are like the given term
     * The results are sorted by relevance (based on name, description, and tags)
     * and then by creation time in descending order.
     *
     * @param term The term to search for, not case-sensitive
     * @return a list of public renovation records that match the term if given
     */
    public Page<RenovationRecordDTO> getPaginatedPublicRecords(String term, List<Tag> tagList, Pageable pageable) {
        Page<RenovationRecord> rawPage;

        if (term.trim().isEmpty() && tagList == null) {
            rawPage = renovationRecordRepository.findPublicRecords(Pageable.unpaged());
        } else if (tagList == null) {
            rawPage = renovationRecordRepository.findPublicRecordsBySearch(term, Pageable.unpaged());
        } else if (term.trim().isEmpty()) {
            rawPage = renovationRecordRepository.findPublicRecordsByTag(tagList, Pageable.unpaged());
        } else {
            rawPage = renovationRecordRepository.findPublicRecordsBySearchOrTag(term, tagList, Pageable.unpaged());
        }

        List<RenovationRecord> sorted = rawPage.getContent().stream()
                .sorted(relevanceComparator(term, tagList))
                .toList();

        return toPage(sorted, pageable);
    }

    /**
     * Retrieves a list of public or users renovation records that are like the given term
     * The results are sorted by relevance (based on name, description, and tags)
     * and then by creation time in descending order.
     *
     * @param term The term to search for, not case-sensitive
     * @return a list of public or users renovation records that match the term if given
     */
    public Page<RenovationRecordDTO> getPaginatedVisibleRecords(User user, String term, List<Tag> tagList, Pageable pageable) {
        Page<RenovationRecord> rawPage;

        if (term.trim().isEmpty() && tagList == null) {
            rawPage = renovationRecordRepository.findVisibleRecords(user, Pageable.unpaged());
        } else if (tagList == null) {
            rawPage = renovationRecordRepository.findVisibleRecordsBySearch(user, term, Pageable.unpaged());
        } else if (term.trim().isEmpty()) {
            rawPage = renovationRecordRepository.findVisibleRecordsByTag(user, tagList, Pageable.unpaged());
        } else {
            rawPage = renovationRecordRepository.findVisibleRecordsBySearchOrTag(user, term, tagList, Pageable.unpaged());
        }

        List<RenovationRecord> sorted = rawPage.getContent().stream()
                .sorted(relevanceComparator(term, tagList))
                .toList();

        return toPage(sorted, pageable);
    }

    /**
     * Calculates a relevance score for a renovation record based on how well it matches
     * the provided search term and tag list. The score increases as follows:
     * <ul>
     *   <li>+1 if the record's name or description contains the search term</li>
     *   <li>+1 for each tag in the tagList that is present in the record's tag list</li>
     * </ul>
     *
     * @param record   The {@link RenovationRecord} to evaluate.
     * @param term     The search term to match against name and description. May be blank.
     * @param tagList  The list of tags to match against the record’s tags. May be null.
     * @return An integer score representing how relevant the record is to the provided term and tags.
     */
    private int calculateRelevance(RenovationRecord record, String term, List<Tag> tagList) {
        int score = 0;
        String lowerTerm = term.toLowerCase();
        if (!term.isBlank()) {
            if (record.getName() != null && record.getName().toLowerCase().contains(lowerTerm)) score++;
            else if (record.getDescription() != null && record.getDescription().toLowerCase().contains(lowerTerm)) score++;
        }
        if (tagList != null && record.getTags() != null) {
            for (Tag tag : tagList) {
                if (record.getTags().contains(tag)) score++;
            }
        }
        return score;
    }

    /**
     * Converts a sorted list of renovation records into a paginated {@link Page} object
     * using the provided {@link Pageable} information.
     *
     * @param sorted   The list of {@link RenovationRecord} objects, already sorted.
     * @param pageable The pagination information including offset and page size.
     * @return A {@link PageImpl} containing the appropriate sublist of the input.
     */
    private Page<RenovationRecordDTO> toPage(List<RenovationRecord> sorted, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), sorted.size());
        List<RenovationRecordDTO> pageContent = (start < end)
                ? sorted.subList(start, end).stream().map(this::toDTO).toList()
                : Collections.emptyList();

        return new PageImpl<>(pageContent, pageable, sorted.size());
    }

    /**
     * Builds a comparator that orders renovation records first by relevance score,
     * then by creation timestamp in descending order.
     *
     * @param term    Search term
     * @param tagList List of tags
     * @return A comparator for sorting renovation records
     */
    private Comparator<RenovationRecord> relevanceComparator(String term, List<Tag> tagList) {
        return Comparator
                .comparingInt((RenovationRecord r) -> calculateRelevance(r, term, tagList)).reversed()
                .thenComparing(RenovationRecord::getCreatedTimestamp, Comparator.nullsLast(Comparator.reverseOrder()));
    }

    /**
     * Converts a {@link RenovationRecord} entity to a {@link RenovationRecordDTO}.
     *
     * @param record The entity to convert
     * @return The corresponding DTO
     */
    private RenovationRecordDTO toDTO(RenovationRecord record) {
        String createdTime = record.getCreatedTimestamp() != null
                ? record.getCreatedTimestamp().toString()
                : "Unknown";

        List<TagDTO> tags = record.getTags() != null
                ? record.getTags().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getTagName()))
                .toList()
                : Collections.emptyList();

        Long userId = record.getUser() != null ? record.getUser().getId() : null;

        return new RenovationRecordDTO(
                record.getId(),
                record.getName(),
                record.getDescription(),
                record.isPublic(),
                createdTime,
                tags,
                userId
        );
    }

    /**
     * Adds a new renovation record to the repository
     *
     * @param renovationRecord the record to be added
     */
    public RenovationRecord addRenovationRecord(RenovationRecord renovationRecord) {
        return renovationRecordRepository.save(renovationRecord);
    }
    /**
     * Removes a renovation record by its id, but first checks it exists.
     * @param id of the record to remove
     */
    @Transactional
    public void removeRenovationRecord(Long id){
        Optional<RenovationRecord> recordToRemove = renovationRecordRepository.findById(id);
        if (recordToRemove.isPresent()) {
            renovationTaskRepository.deleteTaskById(id);
            renovationRecordRepository.deleteById(id);
        }
    }
    /**
     * Changes publicity flag of the renovation record.
     * @param isPublic publicity flag of renovation
     * @param renovationRecord to edit the publicity
     */
    public void changePublicity(Boolean isPublic,RenovationRecord renovationRecord) {
        renovationRecord.setPublicity(isPublic);
        renovationRecordRepository.save(renovationRecord);
    }
    /**
     * Gets a renovation record by its id
     * @param id of the record to get
     * @return the record with the same id
     */
    public RenovationRecord getRecordById(Long id) {
        return renovationRecordRepository.findById(id).orElse(null);
    }

    /**
     * Validates all renovation fields for creating a new renovation record.
     *
     * @param name The name of the renovation to validate.
     * @param description The description of the renovation to validate.
     * @param roomList The list of room names to validate.
     * @return A map of validation errors, where each key is a field name (e.g., "nameError")
     *         and the corresponding value is a list of error messages.
     */
    public Map<String, List<String>> validateAllInputsCreate(String name, String description, List<String> roomList) {
        Map<String, List<String>> errors = new HashMap<>();

        MapUtil.putIfNotEmpty(errors, "nameError", renovationRecordValidation.validateName(name));
        MapUtil.putIfNotEmpty(errors, "nameError", renovationRecordValidation.checkForExactMatchCreate(name));
        MapUtil.putIfNotEmpty(errors, "descriptionError", renovationRecordValidation.validateDescription(description));
        MapUtil.putIfNotEmpty(errors, "roomError", renovationRecordValidation.validateRooms(roomList));
        return errors;
    }

    /**
     * Validates all renovation fields for editing an existing renovation record.
     * Allows the name to match the current name of the provided renovation record.
     *
     * @param renovationRecord The existing renovation record, including its original name, description, and rooms.
     * @param newName The new name to validate.
     * @return A map of validation errors, where each key is a field name (e.g., "nameError")
     *         and the corresponding value is a list of error messages. Returns an empty map if all inputs are valid.
     */
    public Map<String, List<String>> validateAllInputsEdit(RenovationRecord renovationRecord, String newName) {
        Map<String, List<String>> errors = new HashMap<>();

        MapUtil.putIfNotEmpty(errors, "nameError", renovationRecordValidation.validateName(newName));
        MapUtil.putIfNotEmpty(errors, "nameError", renovationRecordValidation.checkForExactMatchEdit(newName, renovationRecord));
        MapUtil.putIfNotEmpty(errors, "descriptionError", renovationRecordValidation.validateDescription(renovationRecord.getDescription()));
        MapUtil.putIfNotEmpty(errors, "roomError", renovationRecordValidation.validateRooms(renovationRecord.getRooms()));
        return errors;
    }


    /**
     * Returns a paginated list of tasks for the given record.
     * @param records The renovation record containing the list of tasks to be paginated.
     * @param pageable spring pagination information, including the offset and page size.
     * @return A page of tasks for the renovation record. If there are no tasks an empty page is returned.
     */
    public Page<RenovationRecord> returnRecordPages(Pageable pageable, List<RenovationRecord> records) {
        List<RenovationRecord> recordsSubList = new ArrayList<>();

        if (records == null || records.isEmpty()) {
            return new PageImpl<>(recordsSubList, pageable, 0); // Return an empty page
        }

        int startIndex =(int) pageable.getOffset();
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (startIndex >= records.size()) {
            startIndex = records.size() - pageable.getPageSize();
        }
        int endIndex = Math.min(startIndex + pageable.getPageSize(), records.size());

        recordsSubList = records.subList(startIndex, endIndex);
        return new PageImpl<>(recordsSubList, pageable, records.size());
    }
}

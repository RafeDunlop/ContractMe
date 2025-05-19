package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
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
     * Retrieves a list of renovation records associated with the current user that are like the given term
     * with pagination.
     * @param user The current user
     * @param term The term to search for, not case-sensitive
     * @param pageable The pagination information
     * @return a list of renovation records from the user that match the term if given
     */
    public Page<RenovationRecord> getPaginatedUserRecords(User user, String term,
                                                        Pageable pageable) {
        if (term == null || term.trim().isEmpty()) {
            return renovationRecordRepository.findByUser(user, pageable);
        }
        return renovationRecordRepository.searchNameOrDescriptionContainingIgnoreCasePaginated(user, term, pageable);
    }

    /**
     * Retrieves a list of renovation records associated with the current user that are like the given term
     * @param user The current user
     * @param term The term to search for, not case-sensitive
     * @return a list of renovation records from the user that match the term if given
     */
    public List<RenovationRecord> getUserRecords(User user, String term) {
        if (term == null || term.trim().isEmpty()) {
            return renovationRecordRepository.findByUser(user);
        }
        return renovationRecordRepository.findByUserTrueSearchContainingNameOrDescriptionIgnoreCase(user, term);
    }

    /**
     * Retrieves a list of public renovation records that are like the given term
     * @param term The term to search for, not case-sensitive
     * @return a list of public renovation records that match the term if given
     */
    public List<RenovationRecord> getPublicRecords(String term) {
        if (term == null || term.trim().isEmpty()) {
            return renovationRecordRepository.findByIsPublicTrue();
        }
        return renovationRecordRepository.findByIsPublicTrueSearchContainingNameOrDescriptionIgnoreCase(term);
    }

    /**
     * Retrieves a list of public or users renovation records that are like the given term
     * @param term The term to search for, not case-sensitive
     * @return a list of public or users renovation records that match the term if given
     */
    public List<RenovationRecord> getAllRecords(User user, String term) {
        if (term == null || term.trim().isEmpty()) {
            return renovationRecordRepository.findAllVisibleToUser(user);
        }
        return renovationRecordRepository.findAllVisibleToUserSearchContainingNameOrDescriptionIgnoreCase(user, term);
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

    /**
     * Retrieves a list of all renovation records that are associated with the given tags.
     * @param tagList the list of tag objects.
     * @return a list of renovation records associated with the tags in the given list.
     */
    public List<RenovationRecord> getAllRecordsByTags(List<Tag> tagList, String visibility, User user, String searchTerm) {
        return switch (visibility) {
            case "all" -> renovationRecordRepository.findAllVisibleToUserSearchContainingNameOrDescriptionAndTagsIgnoreCase(user, searchTerm, tagList);
            case "public" -> renovationRecordRepository.findByIsPublicTrueSearchContainingNameOrDescriptionAndTagsIgnoreCase(searchTerm, tagList);
            case "user" -> renovationRecordRepository.findByUserTrueSearchContainingNameOrDescriptionAndTagsIgnoreCase(user, searchTerm, tagList);

            default -> new ArrayList<>();
        };
    }
}

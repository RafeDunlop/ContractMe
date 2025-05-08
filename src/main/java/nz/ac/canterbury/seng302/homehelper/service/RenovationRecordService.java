package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.util.MapUtil;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationRecordValidation;
import org.springframework.beans.factory.annotation.Autowired;
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
}

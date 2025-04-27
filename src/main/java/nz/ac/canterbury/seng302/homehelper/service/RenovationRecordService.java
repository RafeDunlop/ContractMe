package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
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
    private final RenovationRecordValidation renovationRecordValidation;

    /**
     * Constructor for the RenovationRecordService class
     * @param renovationRecordRepository initializes with the repository for storing records
     */
    @Autowired
    public RenovationRecordService(RenovationRecordRepository renovationRecordRepository, RenovationRecordValidation renovationRecordValidation) {
        this.renovationRecordRepository = renovationRecordRepository;
        this.renovationRecordValidation = renovationRecordValidation;
    }

    /**
     * Retrieves a list of renovation records associated with the current user that are like the given name
     * @param user The current user
     * @param name The name to search for, not case-sensitive
     * @return a list of renovation records from the user that match the name
     */
    public List<RenovationRecord> getRecordResultByName(User user, String name) {
        if (name == null || name.trim().isEmpty()) {
            return renovationRecordRepository.findByUser(user);
        }
        return renovationRecordRepository.findByNameContainingIgnoreCase(user, name);
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
            renovationRecordRepository.deleteById(id);
        }
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

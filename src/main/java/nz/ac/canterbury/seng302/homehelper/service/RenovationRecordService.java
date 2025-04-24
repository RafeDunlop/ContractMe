package nz.ac.canterbury.seng302.homehelper.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs all logic to do with renovation records which does not face the UI
 * and interfaces with the repository
 * @author Jake Connolly
 */
@Service
public class RenovationRecordService {

    private static final Logger logger = LoggerFactory.getLogger(RenovationRecordService.class);
    private static final int maximumDescriptionLength = 512;
    private final RenovationRecordRepository renovationRecordRepository;
    private final LoginService loginService;

    /**
     * Constructor for the RenovationRecordService class
     * @param renovationRecordRepository initializes with the repository for storing records
     */
    @Autowired
    public RenovationRecordService(RenovationRecordRepository renovationRecordRepository, LoginService loginService) {
        this.renovationRecordRepository = renovationRecordRepository;
        this.loginService = loginService;
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
     *         and the corresponding value is a list of error messages. Returns an empty map if all inputs are valid.
     */
    public Map<String, List<String>> validateAllInputsCreate(String name, String description, List<String> roomList) {
        Map<String, List<String>> errors = new HashMap<>();
        Pattern pattern = Pattern.compile("^[\\p{L}\\d ,.\\-']*$", Pattern.UNICODE_CHARACTER_CLASS);

        if (checkForExactMatch(name)) {
            errors.computeIfAbsent("nameError", k -> new ArrayList<>())
                    .add("A renovation with this name already exists.");
        }
        if (name.isEmpty()) {
            errors.computeIfAbsent("nameError", k -> new ArrayList<>())
                    .add("Renovation record name cannot by empty.");
        }
        if (!validateName(name, pattern)) {
            errors.computeIfAbsent("nameError", k -> new ArrayList<>())
                    .add("Renovation record name must only include letters,numbers, spaces, dots, hyphens or apostrophes.");
        }
        if (!validateDescriptionLength(description)) {
            errors.computeIfAbsent("descriptionError", k -> new ArrayList<>())
                    .add("Renovation description must be 512 characters or less and cannot be empty.");
        }
        if (!validateAllRoomNames(roomList, pattern)) {
            errors.computeIfAbsent("roomError", k -> new ArrayList<>())
                    .add("Room names must only contain letters, numbers, spaces, commas, dots, hyphens, or apostrophes.");
        }

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
        Pattern pattern = Pattern.compile("^[\\p{L}\\d ,.\\-']*$", Pattern.UNICODE_CHARACTER_CLASS);

        if (checkForExactMatch(newName, renovationRecord)) {
            errors.computeIfAbsent("nameError", k -> new ArrayList<>())
                    .add("A renovation with this name already exists.");
        }
        if (newName.isEmpty()) {
            errors.computeIfAbsent("nameError", k -> new ArrayList<>())
                    .add("Renovation record name cannot be empty.");
        }
        if (!validateName(newName, pattern)) {
            errors.computeIfAbsent("nameError", k -> new ArrayList<>())
                    .add("Renovation record name must only include letters, numbers, spaces, dots, hyphens or apostrophes.");
        }
        if (!validateDescriptionLength(renovationRecord.getDescription())) {
            errors.computeIfAbsent("descriptionError", k -> new ArrayList<>())
                    .add("Renovation description must be 512 characters or less and cannot be empty.");
        }
        if (!validateAllRoomNames(renovationRecord.getRooms(), pattern)) {
            errors.computeIfAbsent("roomError", k -> new ArrayList<>())
                    .add("Room names must only contain letters, numbers, spaces, commas, dots, hyphens, or apostrophes.");
        }

        return errors;
    }


    /**
     * Calls other functions to validate all renovation fields, returning false if any do not pass their validity checks. Predicate for
     * name is tested which calls the checkForExactMatch and validateName functions.
     * @param name Name of the record
     * @param description Description for the record
     * @param roomList List of rooms for the record
     * @param nameChecker A predicate which checks if the name doesn't exist and if it follows the correct string pattern
     * @param pattern The string pattern the list of rooms must follow
     * @return A boolean whether all the details are in the correct format and are valid
     */
    private boolean validateAllInputs(String name, String description, List<String> roomList, Predicate<String> nameChecker, Pattern pattern) {
        return (validateAllRoomNames(roomList, pattern) &&
                validateDescriptionLength(description) &&
                nameChecker.test(name));
    }

    /**
     * Checks whether there is a saved renovation with the specified name in the database
     * <strong>
     *     This version of the method passes names which are identical to that of the specified
     *     {@code RenovationRecord}. <u>This means that the name field of the {@code RenovationRecord}
     *     must not be updated before saving the renovation</u>
     * </strong>
     * @param name The name to check
     * @param renovationRecord The renovation whose name is always valid
     * @return true if there is no match or the match is the specified {@code RenovationRecord}
     */
    public boolean checkForExactMatch(String name, RenovationRecord renovationRecord) {
        return checkForExactMatch(name) && !renovationRecord.getName().equals(name);
    }

    /**
     * Validates the specified renovation name based on the specified pattern in addition to the mandatory condition
     * that it must not be empty
     * @param name The name to be checked
     * @param pattern the pattern against which to check the renovation name
     * @return true if the specified name matches the specified pattern and is non-empty
     */
    public boolean validateName(String name, Pattern pattern) {
        return pattern.matcher(name).matches();
    }

    /**
     * Runs the specified validity pattern on each specified room, returning {@code false} if any room
     * is not valid
     * @param roomNames a list of rooms to check
     * @param validityPattern A regular expression asserting the format which a room should follow
     * @return {@code false} if any room is invalid, otherwise true
     */
    public boolean validateAllRoomNames(List<String> roomNames, Pattern validityPattern) {
        for (String room : roomNames) {
            Matcher matcher = validityPattern.matcher(room);
            if (!matcher.matches()) {
                logger.info("{} does not match", room);
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that the specified description is within a valid length range
     * Note that no conditions are placed on the contents of the description
     * @param description The description to be tested
     * @return {@code true} if the description is less than the maximum description length
     */
    public boolean validateDescriptionLength(String description) {
        return description.length() <= maximumDescriptionLength;
    }

    /**
     * Checks if a name has an exact match in the repository with the logged-in user
     * @param name the name to check to see if its present in the repository
     * @return true if a match for the name is found, otherwise false
     */
    public boolean checkForExactMatch(String name) {
        return renovationRecordRepository.findExactMatch(name.trim(), loginService.getUserByEmail()).isPresent();
    }
}

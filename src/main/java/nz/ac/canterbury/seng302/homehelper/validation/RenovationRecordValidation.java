package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validation class for renovation record details.
 */
@Service
public class RenovationRecordValidation {
    private final RenovationRecordRepository renovationRecordRepository;
    private final LoginService loginService;
    private static final int maximumDescriptionLength = 512;

    /**
     * Constructor for the RenovationRecordValidation class
     * @param renovationRecordRepository initializes with the repository for storing records
     */
    @Autowired
    public RenovationRecordValidation(RenovationRecordRepository renovationRecordRepository, LoginService loginService) {
        this.renovationRecordRepository = renovationRecordRepository;
        this.loginService = loginService;
    }

    /**
     * Checks if a name has an exact match in the repository with the logged-in user
     * @param name the name to check to see if its present in the repository
     * @return true if a match for the name is found, otherwise false
     */
    public List<String> checkForExactMatchCreate(String name) {
        List<String> errors = new ArrayList<>();
        if (renovationRecordRepository.findExactMatch(name.trim(), loginService.getUserByEmail()).isPresent()) {
            errors.add("A renovation with this name already exists.");
        }
        return errors;
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
    public List<String> checkForExactMatchEdit(String name, RenovationRecord renovationRecord) {
        List<String> errors = new ArrayList<>();
        var existing = renovationRecordRepository.findExactMatch(name.trim(), loginService.getUserByEmail());

        if (existing.isPresent() && !existing.get().getId().equals(renovationRecord.getId())) {
            errors.add("A renovation with this name already exists.");
        }

        return errors;
    }


    /**
     * Validates the specified renovation name based on the specified pattern in addition to the mandatory condition
     * that it must not be empty
     *
     * @param name The name to be checked
     * @return true if the specified name matches the specified pattern and is non-empty
     */
    public List<String> validateName(String name) {
        List<String> errors = new ArrayList<>();
        Pattern pattern = Pattern.compile("^[\\p{L}\\d ,.\\-']*$", Pattern.UNICODE_CHARACTER_CLASS);
        if (!pattern.matcher(name).matches()) {
            errors.add("Renovation record room names must only include letters, numbers, spaces, dots, hyphens or apostrophes.");
        }

        if (name.trim().isEmpty()) {
            errors.add("Renovation record name cannot be empty.");
        }
        return errors;
    }

    /**
     * Runs the specified validity pattern on each specified room, returning {@code false} if any room
     * is not valid
     * @param roomNames a list of rooms to check
     * @return {@code false} if any room is invalid, otherwise true
     */
    public List<String> validateRooms(List<String> roomNames) {
        List<String> errors = new ArrayList<>();
        Pattern pattern = Pattern.compile("^[\\p{L}\\d ,.\\-']*$", Pattern.UNICODE_CHARACTER_CLASS);
        for (String room : roomNames) {
            Matcher matcher = pattern.matcher(room);
            if (!matcher.matches()) {
                errors.add("Room names must only contain letters, numbers, spaces, commas, dots, hyphens, or apostrophes.");
                break;
            }
        }
        return errors;
    }

    /**
     * Checks that the specified description is within a valid length range
     * Note that no conditions are placed on the contents of the description
     * @param description The description to be tested
     * @return {@code true} if the description is less than the maximum description length
     */
    public List<String> validateDescription(String description) {
        List<String> errors = new ArrayList<>();
        if (description.length() > maximumDescriptionLength) {
            errors.add("Renovation record description must be 512 characters or less.");
        }
         return errors;
    }
}

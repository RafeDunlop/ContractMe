package nz.ac.canterbury.seng302.homehelper.validation;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Validation class for task details.
 */
public class TaskValidation {

    private static final int maximumDescriptionLength = 512;

    /**
     * Validates the details of the task inputted by the user. Checks to see if all the details are valid and returns a list
     * of error messages for each invalid detail.
     * @param name Name of the task
     * @param description Description for the task
     * @param roomList List of rooms for the task
     * @param dueDate Due date of the task
     * @return A list of errors generated from validating the task details
     */
    public List<String> validateTaskDetails(String name, String description, List<String> roomList, LocalDateTime dueDate) {
        List<String> errors = new ArrayList<>();
        String errorMessageType = "Task";
        String nameErrors, descriptionErrors, dueDateError;
        if ((nameErrors = validateName(name, errorMessageType)) != null) {
            errors.add(nameErrors);
        }
        if ((descriptionErrors = validateDescription(description, errorMessageType)) != null) {
            errors.add(descriptionErrors);
        }
        if ((dueDateError = validateDueDate(dueDate)) != null) {
            errors.add(dueDateError);
        }
        return errors;
    }

    /**
     * Validates the name of an object and returns an error message if it's invalid.
     * @param name Name of the object being verified
     * @param errorMessageType String added to the start of the error message
     * @return An error from validating the name
     */
    private String validateName(String name, String errorMessageType) {
        // Regex specifies that names must only contain letters (from any language), spaces, dots, hyphens, and/or apostrophes.
        if (name.trim().isEmpty() || !name.matches("^[\\p{L}\\s\\-'.]*$")) {
            return StringUtils.capitalize(errorMessageType +  (" name cannot be empty and must only include letters, numbers, " +
                    "spaces, dots, hyphens or apostrophes").trim());
        }
        return null;
    }

    /**
     * Validates the description of an object and returns an error message if it's invalid.
     * @param description Description for the object being verified
     * @param errorMessageType String added to the start of the error message
     * @return An error from validating the description
     */
    private String validateDescription(String description, String errorMessageType) {
        if (description.length() > maximumDescriptionLength) {
            return StringUtils.capitalize(errorMessageType + " description must be 512 characters or less".trim());
        } else if (description.trim().isEmpty()) {
            return StringUtils.capitalize(errorMessageType + " description cannot be empty".trim());
        }
        return null;
    }

    /**
     * Validates the due date of an object and returns an error message if it's invalid.
     * @param dueDate Due date of the object being verified
     * @return An error from validating the due date
     */
    private String validateDueDate(LocalDateTime dueDate) {
        if (dueDate.isBefore(LocalDateTime.now())) {
            return "Due date must be in the future";
        }
        return null;
    }
}
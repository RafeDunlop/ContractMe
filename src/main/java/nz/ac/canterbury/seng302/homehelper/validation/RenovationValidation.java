package nz.ac.canterbury.seng302.homehelper.validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;

/**
 * Validation class for task details.
 */
@Service
public class RenovationValidation {
    private static final String ICON_DIR = "/static/images/";
    private static final int maximumDescriptionLength = 512;

    /**
     * Validates the details of the task inputted by the user. Checks to see if all the details are valid and returns a list
     * of error messages for each invalid detail.
     * @return A list of errors generated from validating the task details
     */
    public List<String> validateTaskDetails(RenovationTaskDTO renovationTaskDTO) {
        List<String> errors = new ArrayList<>();
        String errorMessageType = "Task";
        String nameErrors, descriptionErrors, dueDateError;
        if ((nameErrors = validateName(renovationTaskDTO.getName(), errorMessageType)) != null) {
            errors.add(nameErrors);
        }
        if ((descriptionErrors = validateDescription(renovationTaskDTO.getDescription(), errorMessageType)) != null) {
            errors.add(descriptionErrors);
        }
        if ((dueDateError = validateDueDate(renovationTaskDTO.getDueDate())) != null) {
            errors.add(dueDateError);
        }
        return errors;
    }

    /**
     * Checks if the icon file exists and is not a directory.
     *
     * File existence checking code taken from stack overflow answer (CC-BY-SA 4.0):
     * <a href="https://stackoverflow.com/questions/9380690/how-to-see-if-resource-file-exists-in-java">here</a>,
     * author <a href="https://stackoverflow.com/users/416564/johan-sj%c3%b6berg">Johan Sjöberg</a>,
     *
     * @param fileName The file to check.
     * @return true if the file does not exist and is not a directory, false otherwise.
     */
    public boolean validateTaskIconFileName(String fileName) {
        Resource resource = new ClassPathResource(ICON_DIR + fileName);
        return (resource.exists());
    }

    /**mappedBy = "renovationRecord"
     * Validates the name of an object and returns an error message if it's invalid.
     * @param name Name of the object being verified
     * @param errorMessageType String added to the start of the error message
     * @return An error from validating the name
     */
    private String validateName(String name, String errorMessageType) {
        // Regex specifies that names must only contain letters (from any language), numbers, spaces, dots, hyphens, and/or apostrophes.
        if (name.trim().isEmpty() || !name.matches("^[\\p{L}0-9\\s\\-'.]*$")) {
            return StringUtils.capitalize((errorMessageType +  (" name cannot be empty and must only include letters, numbers, " +
                    "spaces, dots, hyphens or apostrophes.")).trim());
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
            return StringUtils.capitalize((errorMessageType + " description must be 512 characters or less.").trim());
        } else if (description.trim().isEmpty()) {
            return StringUtils.capitalize((errorMessageType + " description cannot be empty.").trim());
        }
        return null;
    }

    /**
     * Validates the due date of an object and returns an error message if it's invalid.
     * @param dueDate Due date of the object being verified
     * @return An error from validating the due date
     */
    private String validateDueDate(LocalDate dueDate) {
        if (dueDate != null) {
            if (dueDate.isBefore(LocalDate.now())) {
                return "Due date must be in the future.";
            }
        }
            return null;

    }
}

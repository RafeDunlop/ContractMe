package nz.ac.canterbury.seng302.homehelper.validation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;

/**
 * Validation class for task details.
 */
@Service
public class RenovationTaskValidation {
    private static final String ICON_DIR = "/static/images/";
    private static final int maximumDescriptionLength = 512;

    /**
     * Checks if the icon file existsy.
     *
     * @param fileName The file to check.
     * @return true if the file does not existy, false otherwise.
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
    public String validateName(String name, String errorMessageType) {
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
    public String validateDescription(String description, String errorMessageType) {
        if (description.length() > maximumDescriptionLength) {
            return StringUtils.capitalize((errorMessageType + " description must be 512 characters or less.").trim());
        } else if (description.trim().isEmpty()) {
            return StringUtils.capitalize((errorMessageType + " description cannot be empty.").trim());
        }
        return null;
    }

    public String validateRooms(RenovationRecord renovation, List<String> rooms) {
        for (String room : rooms) {
            if (!renovation.getRooms().contains(room)) {
                return StringUtils.capitalize((String.format(
                        "Whoops, it looks like \"%s\" is not a valid room anymore", room
                )).trim());
            }
        }
        return null;
    }

    /**
     * Validates the due date of an object and returns an error message if it's invalid.
     * @param dueDate Due date of the object being verified
     * @return An error from validating the due date
     */
    public String validateDueDate(LocalDate dueDate) {
        if (dueDate != null) {
            if (dueDate.isBefore(LocalDate.now())) {
                return "Due date must be in the future.";
            }
        }
            return null;

    }
}

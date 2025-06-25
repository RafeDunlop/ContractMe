package nz.ac.canterbury.seng302.homehelper.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

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
     * @return A list of errors from validating the name
     */
    public List<String> validateName(String name, String errorMessageType) {
        List<String> errors = new ArrayList<>();

        String trimmedName = name == null ? "" : name.trim();

        if (trimmedName.isEmpty() || !trimmedName.matches("^[\\p{L}0-9\\s\\-'.]*$")) {
            errors.add(StringUtils.capitalize(errorMessageType + " name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes."));
        }

        if (trimmedName.length() > 128) {
            errors.add(StringUtils.capitalize(errorMessageType + " name cannot be greater than 128 characters."));
        }

        return errors;
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

    /**
     * Ensures that all the rooms specified belong to the {@link RenovationRecord} specified.
     * @param renovation The {@link RenovationRecord} which should contain all specified rooms
     * @param rooms The rooms to check
     * @return null if the parameters comply, otherwise a readable error corresponding to the first room which does not match
     */
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
     * @param dueDateString Due date of the object being verified
     * @return An error from validating the due date
     */
    public String validateDueDate(String dueDateString) {


        if (dueDateString != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dueDate = LocalDate.parse(dueDateString, formatter);
                if (dueDate.isBefore(LocalDate.now())) {
                    return "Due date must be in the future.";
                }
            } catch (DateTimeParseException e) {
                return "Date is not in valid format, DD/MM/YYYY";
            }

        }
            return null;

    }
}

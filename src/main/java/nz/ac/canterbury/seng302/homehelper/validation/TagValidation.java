package nz.ac.canterbury.seng302.homehelper.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
/**
 * Validation class for tag details.
 */
@Service
public class TagValidation {
    /**
     * Validates the tag name
     * @param tagName the tag name to validate
     * @return  A list of errors from validating the tag name
     */
    public List<String> validateName(String tagName) {
        List<String> errors = new ArrayList<>();

        if (tagName.isEmpty() || !tagName.matches("^(?=.*\\p{L}).*$")) {
            errors.add(StringUtils.capitalize("Tags must contain one or more letters."));
        }
        if (tagName.length() > 128) {
            errors.add(StringUtils.capitalize("Tag cannot be greater than 128 characters."));
        }
        return errors;
    }
}

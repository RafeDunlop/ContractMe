package nz.ac.canterbury.seng302.homehelper.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagValidation {
    public List<String> validateName(String tagName) {
        List<String> errors = new ArrayList<>();

        if (tagName.isEmpty() || !tagName.matches("^(?=.*\\p{L}).*$")) {
            errors.add(StringUtils.capitalize("tags must contain letters"));
        }
        if (tagName.length() > 255) {
            errors.add(StringUtils.capitalize("tag cannot be greater than 128 characters."));
        }
        return errors;
    }
}

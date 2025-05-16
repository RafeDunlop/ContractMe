package nz.ac.canterbury.seng302.homehelper.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class LocationValidation {

    /**
     * Validates city suburb by ensuring suburb name string
     * doesn't contain any invalid characters
     * (i.e. characters others than letters, hyphen, apostrophe, number, space, dot)
     * @param suburb suburb name to be verified
     * @return errors; list of errors found during validation
     */
    public List<String> validateSuburb(String suburb) {
        List<String> errors = new ArrayList<>();

        if (!suburb.matches("^[\\p{L}\\d\\-\\s']*$")) {
            errors.add("Suburb must only include letters, spaces, hyphens, digits or apostrophes.");
        }

        return errors;
    }

    /**
     * Validates street address by checking for invalid characters
     * @param address the street address (with number)
     * @return errors list of errors found during validation
     */
    public List<String> validateStreetAddress(String address) {
        List<String> errors = new ArrayList<>();
        if (!address.matches("^[\\p{L}\\-'\\d\\s.]*$")) {
            errors.add("Street address contains invalid characters.");
        }
        return errors;
    }

}

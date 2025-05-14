package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
            errors.add("Suburb contains invalid characters.");
        }

        return errors;
    }

}

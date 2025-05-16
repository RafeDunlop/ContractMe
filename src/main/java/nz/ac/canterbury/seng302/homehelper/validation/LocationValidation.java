package nz.ac.canterbury.seng302.homehelper.validation;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
     * Validates the specified city name based on the specified pattern in addition to the mandatory condition
     * that it must not be empty
     *
     * @param city The city name to be checked
     * @return true if the specified city name matches the specified pattern and is non-empty
     */
    public List<String> validateCity(String city) {
        List<String> errors = new ArrayList<>();
        Pattern pattern = Pattern.compile("^[\\p{L} \\-']+$", Pattern.UNICODE_CHARACTER_CLASS);
        String cityName = city.trim();
        if (!pattern.matcher(cityName).matches()) {
            errors.add("City contains invalid characters.");
        }

        return errors;
    }


    /**
     * Validates the postcode based on the specified pattern
     * @param postcode The postcode
     * @return error list
     */
    public List<String> validatePostcode(String postcode) {
        List<String> errors = new ArrayList<>();

        if (postcode == null || postcode.trim().isEmpty()) {
            errors.add("Postcode cannot be empty.");
        } else {
            String trimmed = postcode.trim();

            if (!trimmed.matches("^[\\p{L}\\p{N} ]+$")) {
                errors.add("Postcode contains invalid characters.");
            } else if (trimmed.chars().filter(c -> c == ' ').count() > 1) {
                errors.add("Postcode contains invalid characters.");
            }
        }

        return errors;
    }


    /**
     * Validates the postcode based on the specified pattern
     * @param country The country
     * @return list of errors
     */
    public List<String> validateCountry(String country) {
        List<String> errors = new ArrayList<>();

        String trimmed = country.trim();

        if (!trimmed.matches("^[\\p{L}\\p{N} ]+$")) {
            errors.add("Postcode contains invalid characters.");
        } else if (trimmed.chars().filter(c -> c == ' ').count() > 1) {
            errors.add("Postcode contains invalid characters.");
        }

        return errors;
    }


}

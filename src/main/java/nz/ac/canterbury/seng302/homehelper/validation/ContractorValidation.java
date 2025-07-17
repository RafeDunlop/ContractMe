package nz.ac.canterbury.seng302.homehelper.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for contractor validation
 */
public class ContractorValidation {


    public List<String> validateHourlyRate(Float hourlyRate) {
        List<String> errors = new ArrayList<>();

        if (hourlyRate < 0) {
            errors.add("Hourly rate cannot be negative");
        }
        return errors;
    }

    public List<String> validatePhoneNumber(String phoneNumber) {
        List<String> errors = new ArrayList<>();
        String regex = "\\d+";
        if (phoneNumber.length() < 8 || phoneNumber.length() > 15) {
            errors.add("Phone number must be between 8 and 15 digits");
        }

        if (!phoneNumber.matches(regex)) {
            errors.add("Phone number must be a number");
        }

        return errors;
    }


}

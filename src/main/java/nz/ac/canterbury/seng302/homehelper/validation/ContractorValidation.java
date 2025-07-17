package nz.ac.canterbury.seng302.homehelper.validation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for contractor validation
 */
@Service
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

        if (phoneNumber.isBlank()) {
            errors.add("You must enter a valid phone number");
        } else if (phoneNumber.length() < 8 || phoneNumber.length() > 15 || !phoneNumber.matches(regex)) {
            errors.add("Your phone number is invalid");
        }

        return errors;
    }


}

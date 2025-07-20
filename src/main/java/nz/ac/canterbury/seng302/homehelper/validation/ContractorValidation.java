package nz.ac.canterbury.seng302.homehelper.validation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for contractor validation
 */
@Service
public class ContractorValidation {

    /**
     * Validate the hourly rate field, this must be a positive float.
     *
     * @param hourlyRate the contractor's hourly rate
     * @return a list of errors, empty if valid
     */
    public List<String> validateHourlyRate(Float hourlyRate) {
        List<String> errors = new ArrayList<>();

        if (hourlyRate == null || hourlyRate < 0) {
            errors.add("Invalid hourly rate");
        }
        return errors;
    }

    /**
     * Validate the phone number field, this must be a valid numerical string between 8 and 15 digits.
     * It is also a mandatory field
     *
     * @param phoneNumber the contractor's phone number
     * @return a list of errors, empty if valid
     */
    public List<String> validatePhoneNumber(String phoneNumber, Integer countryCode) {
        List<String> errors = new ArrayList<>();
        String regex = "\\d+";

        if (phoneNumber.isBlank()) {
            errors.add("You must enter a valid phone number");
        } else if (phoneNumber.length() < 8 || phoneNumber.length() > 15 || !phoneNumber.matches(regex)) {
            errors.add("Your phone number is invalid");
        }

        errors.addAll(validateCountryCode(countryCode));

        return errors;
    }

    /**
     * Validate the phone number's country code, this must be a positive number 3 digits or fewer.
     *
     * @param countryCode the phone number country code
     * @return a list of errors, empty if valid
     */
    private List<String> validateCountryCode(Integer countryCode) {
        List<String> errors = new ArrayList<>();

        if (countryCode == null || countryCode <= 0 || countryCode > 999) {
            errors.add("Invalid country code");
        }
        return errors;
    }

}

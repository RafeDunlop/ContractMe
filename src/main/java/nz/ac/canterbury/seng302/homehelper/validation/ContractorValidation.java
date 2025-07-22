package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
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
            errors.add("You must enter a phone number");
        } else if (phoneNumber.length() < 8 || phoneNumber.length() > 15 || !phoneNumber.matches(regex)) {
            errors.add("Your phone number is invalid");
        }

        errors.addAll(validateCountryCode(countryCode));

        return errors;
    }


    /**
     * Validate that the location has been provided, this method should be passed
     * the result of {@link nz.ac.canterbury.seng302.homehelper.service.LocationService#isLocationProvided(AddressDTO)}.
     *
     * @param locationProvided a boolean specifying whether the location was provided
     * @return a list of error messages, empty if valid
     */
    public List<String> validateContractorLocation(boolean locationProvided) {
        List<String> errors = new ArrayList<>();
        if (!locationProvided) {
            errors.add("You must enter a location");
        }

        return errors;
    }

    /**
     * Validates that skill inputs contain at least one skill
     * @param skills The list of skills selected
     * @return The errors present, or an empty list if there are not any
     */
    public List<String> validateContractorSkillsField(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) return List.of("You must select one or more skills");
        return List.of();
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

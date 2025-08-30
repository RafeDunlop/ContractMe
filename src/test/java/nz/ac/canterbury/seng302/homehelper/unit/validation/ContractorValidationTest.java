package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.validation.ContractorValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

public class ContractorValidationTest {


    private static Stream<Float> streamInvalidHourlyRates() {
        return Stream.of(-1f, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0226450022","12345678","123456789123456"})
    public void phoneNumberValidation_validPhoneNumber_acceptInput(String phoneNumber) {
        ContractorValidation contractorValidation = new ContractorValidation();
        Assertions.assertTrue(contractorValidation.validatePhoneNumber(phoneNumber, 64).isEmpty());

    }

    @ParameterizedTest
    @ValueSource(strings = {"123","abc","123456789123456789","a1c"})
    public void phoneNumberValidation_invalidPhoneNumber_rejectInput(String phoneNumber) {
        ContractorValidation contractorValidation = new ContractorValidation();
        List <String> result = contractorValidation.validatePhoneNumber(phoneNumber, 64);
        Assertions.assertTrue(result.contains("Your phone number is invalid"));
    }

    @ParameterizedTest
    @ValueSource(strings = {""," "})
    public void phoneNumberValidation_emptyPhoneNumber_rejectInput(String phoneNumber) {
        ContractorValidation contractorValidation = new ContractorValidation();
        List <String> result = contractorValidation.validatePhoneNumber(phoneNumber, 64);
        Assertions.assertTrue(result.contains("You must enter a phone number"));
    }

    @ParameterizedTest
    @MethodSource("streamInvalidHourlyRates")
    public void hourlyRateValidation_invalidHourlyRate_rejectInput(Float hourlyRate) {
        ContractorValidation contractorValidation = new ContractorValidation();
        List<String> result = contractorValidation.validateHourlyRate(hourlyRate);
        List<String> expected = List.of("Invalid hourly rate");
        Assertions.assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(floats = {27.8f, 1, 1000000000, 3.4028235E38f})
    public void hourlyRateValidation_validHourlyRate_acceptInput(float hourlyRate) {
        ContractorValidation contractorValidation = new ContractorValidation();
        Assertions.assertTrue(contractorValidation.validateHourlyRate(hourlyRate).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 64, 999})
    public void countryCodeValidation_validCountryCode_acceptInput(int countryCode) {
        ContractorValidation contractorValidation = new ContractorValidation();
        Assertions.assertTrue(contractorValidation.validatePhoneNumber("12345678", countryCode).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -123, 1000})
    public void countryCodeValidation_invalidCountryCode_rejectInput(int countryCode) {
        ContractorValidation contractorValidation = new ContractorValidation();
        List<String> result = contractorValidation.validatePhoneNumber("12345678", countryCode);
        List<String> expected = List.of("Invalid country code");
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void validateLocation_locationProvided_acceptInput() {
        ContractorValidation contractorValidation = new ContractorValidation();
        List<String> result = contractorValidation.validateContractorLocation(true);
        Assertions.assertEquals(0, result.size());
    }


    @Test
    public void validateLocation_locationNotProvided_rejectInput() {
        ContractorValidation contractorValidation = new ContractorValidation();
        List<String> result = contractorValidation.validateContractorLocation(false);
        List<String> expected = List.of("You must enter a location");
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void validateLocation_skillsNotProvided_rejectInput() {
        ContractorValidation contractorValidation = new ContractorValidation();
        List<String> result = contractorValidation.validateContractorSkillsField(List.of());
        List<String> expected = List.of("You must select one or more skills");
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void validateLocation_skillsAreNull_rejectInput() {
        ContractorValidation contractorValidation = new ContractorValidation();
        List<String> result = contractorValidation.validateContractorSkillsField(null);
        List<String> expected = List.of("You must select one or more skills");
        Assertions.assertEquals(expected, result);
    }
}

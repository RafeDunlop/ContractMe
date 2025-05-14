package nz.ac.canterbury.seng302.homehelper.unit.validation;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LocationValidationTest {

    private static LocationValidation locationValidation;

    @BeforeAll
    static void validatorSetup() {
        locationValidation = new LocationValidation();
    }


    @Test
    void testValidateName_invalidCharacterCity_Error() {
        List<String> result = locationValidation.validateCity("Chr%stchurch");
        List<String> secondResult = locationValidation.validateCity("Paris123");
        List<String> thirdResult = locationValidation.validateCity("Wellington (NZ)");
        assertTrue(result.get(0).contains("City contains invalid characters"));
        assertTrue(secondResult.get(0).contains("City contains invalid characters"));
        assertTrue(thirdResult.get(0).contains("City contains invalid characters"));}

    @Test
    void testValidateName_invalidNumberInCity_Error() {
        List<String> result = locationValidation.validateCity("Paris123");
        List<String> secondResult = locationValidation.validateCity("23Kathmandu");
        List<String> thirdResult = locationValidation.validateCity("Auc23kland");
        assertTrue(result.get(0).contains("City contains invalid characters"));
        assertTrue(secondResult.get(0).contains("City contains invalid characters"));
        assertTrue(thirdResult.get(0).contains("City contains invalid characters"));
    }

    @Test
    void testValidateName_invalidTabInCity_Error() {
        List<String> result = locationValidation.validateCity("New\tDelhi");
        assertTrue(result.get(0).contains("City contains invalid characters"));
    }

    @Test
    void testValidateName_validCity_noError() {
        List<String> result = locationValidation.validateCity("Christchurch");
        List<String> secondResult = locationValidation.validateCity("Berlin");
        assertTrue(result.isEmpty());
        assertTrue(secondResult.isEmpty());

    }
    @Test
    void testValidateName_validCharacterCity_noError() {
        List<String> result = locationValidation.validateCity("Saint-Pierre");
        List<String> secondResult = locationValidation.validateCity("O'Connell");
        List<String> thirdResult = locationValidation.validateCity("Tórshavn");
        assertTrue(result.isEmpty());
        assertTrue(secondResult.isEmpty());
        assertTrue(thirdResult.isEmpty());
    }

    @Test
    void testValidateName_validSpaceCity_noError() {
        List<String> result = locationValidation.validateCity("New Delhi");
        List<String> secondResult = locationValidation.validateCity("New York City");;
        assertTrue(result.isEmpty());
        assertTrue(secondResult.isEmpty());
    }


    @Test
    void invalidPostcode_nullPostcode_returnsEmptyError() {
        List<String> result = locationValidation.validatePostcode(null);
        assertTrue(result.contains("Postcode cannot be empty."));
    }

    @Test
    void invalidPostcode_emptyPostcode_returnsEmptyError() {
        List<String> result = locationValidation.validatePostcode("");
        assertTrue(result.contains("Postcode cannot be empty."));
    }

    @Test
    void invalidPostcode_postcodeWithSpecialCharacters_returnsInvalidCharacterError() {
        List<String> result = locationValidation.validatePostcode("123$%");
        assertTrue(result.contains("Postcode contains invalid characters."));
    }

    @Test
    void invalidPostcode_postcodeWithMultipleSpaces_returnsInvalidCharacterError() {
        List<String> result = locationValidation.validatePostcode("12 34 56");
        assertTrue(result.contains("Postcode contains invalid characters."));
    }

    @Test
    void invalidPostcode_postcodeWithTab_returnsInvalidCharacterError() {
        List<String> result = locationValidation.validatePostcode("12\t34");
        assertTrue(result.contains("Postcode contains invalid characters."));
    }

    @Test
    void invalidPostcode_postcodeWithNewline_returnsInvalidCharacterError() {
        List<String> result = locationValidation.validatePostcode("123\n456");
        assertTrue(result.contains("Postcode contains invalid characters."));
    }

    @Test
    void validPostcode_numericPostcode_isValid() {
        List<String> result = locationValidation.validatePostcode("8011");
        assertTrue(result.isEmpty());
    }

    @Test
    void validPostcode_alphanumericPostcode_isValid() {
        List<String> result = locationValidation.validatePostcode("A1B2C3");
        assertTrue(result.isEmpty());
    }

    @Test
    void validPostcode_postcodeWithSingleSpace_isValid() {
        List<String> result = locationValidation.validatePostcode("A1 234");
        assertTrue(result.isEmpty());
    }

    @Test
    void validPostcode_postcodeWithUnicodeLetters_isValid() {
        List<String> result = locationValidation.validatePostcode("Tór4");
        assertTrue(result.isEmpty());
    }

    @Test
    void validPostcode_postcodeWithLeadingAndTrailingSpaces_isTrimmedAndValid() {
        List<String> result = locationValidation.validatePostcode(" 1234 ");
        assertTrue(result.isEmpty());
    }
}

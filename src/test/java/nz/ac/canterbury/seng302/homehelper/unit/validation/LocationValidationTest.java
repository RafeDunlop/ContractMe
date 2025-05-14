package nz.ac.canterbury.seng302.homehelper.unit.validation;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class LocationValidationTest {

    private static LocationValidation locationValidation;

    @BeforeAll
    static void validatorSetup() {
        locationValidation = new LocationValidation();
    }


    @Test
    void testValidateName_invalidCharacterName_Error() {
        List<String> result = locationValidation.validateCity("Chr%stchurch");
        List<String> secondResult = locationValidation.validateCity("Paris123");
        List<String> thirdResult = locationValidation.validateCity("Wellington (NZ)");
        assertTrue(result.get(0).contains("City contains invalid characters"));
        assertTrue(secondResult.get(0).contains("City contains invalid characters"));
        assertTrue(thirdResult.get(0).contains("City contains invalid characters"));}

    @Test
    void testValidateName_invalidNumberInName_Error() {
        List<String> result = locationValidation.validateCity("Paris123");
        List<String> secondResult = locationValidation.validateCity("23Kathmandu");
        List<String> thirdResult = locationValidation.validateCity("Auc23kland");
        assertTrue(result.get(0).contains("City contains invalid characters"));
        assertTrue(secondResult.get(0).contains("City contains invalid characters"));
        assertTrue(thirdResult.get(0).contains("City contains invalid characters"));
    }

    @Test
    void testValidateName_validName_noError() {
        List<String> result = locationValidation.validateCity("Christchurch");
        List<String> secondResult = locationValidation.validateCity("Berlin");
        assertTrue(result.isEmpty());
        assertTrue(secondResult.isEmpty());

    }
    @Test
    void testValidateName_validCharacterName_noError() {
        List<String> result = locationValidation.validateCity("Saint-Pierre");
        List<String> secondResult = locationValidation.validateCity("O'Connell");
        List<String> thirdResult = locationValidation.validateCity("Tórshavn");
        assertTrue(result.isEmpty());
        assertTrue(secondResult.isEmpty());
        assertTrue(thirdResult.isEmpty());
    }

    @Test
    void testValidateName_validSpaceName_noError() {
        List<String> result = locationValidation.validateCity("New Delhi");
        List<String> secondResult = locationValidation.validateCity("New York City");
        assertTrue(result.isEmpty());
        assertTrue(secondResult.isEmpty());
    }



}

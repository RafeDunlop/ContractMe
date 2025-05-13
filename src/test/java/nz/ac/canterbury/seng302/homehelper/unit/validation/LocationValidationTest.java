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
    void testValidateName_invalidName_noError() {
        List<String> result = locationValidation.validateCity("Chr%stchurch");
        List<String> secondResult = locationValidation.validateCity("");
        List<String> thirdResult = locationValidation.validateCity("Paris123");
        List<String> fourthResult = locationValidation.validateCity("Wellington (NZ)");
        List<String> fifthResult = locationValidation.validateCity("Tokyo*");
        assertTrue(result.get(0).contains("City contains invalid characters"));
        assertTrue(secondResult.get(0).contains("City cannot be empty."));
        assertTrue(thirdResult.get(0).contains("City contains invalid characters"));
        assertTrue(fourthResult.get(0).contains("City contains invalid characters"));
        assertTrue(fifthResult.get(0).contains("City contains invalid characters"));
    }
    @Test
    void testValidateName_validName_noError() {
        List<String> firstResult = locationValidation.validateCity("Christchurch");
        List<String> secondResult = locationValidation.validateCity("New York City");
        List<String> thirdResult = locationValidation.validateCity("Saint-Pierre");
        List<String> fourthResult = locationValidation.validateCity("O'Connell");
        List<String> fifthResult = locationValidation.validateCity("Tórshavn");
        assertTrue(firstResult.isEmpty());
        assertTrue(secondResult.isEmpty());
        assertTrue(thirdResult.isEmpty());
        assertTrue(fourthResult.isEmpty());
        assertTrue(fifthResult.isEmpty());

    }

}

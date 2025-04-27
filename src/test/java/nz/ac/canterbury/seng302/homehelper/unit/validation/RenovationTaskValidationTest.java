package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RenovationTaskValidationTest {

    private static RenovationTaskValidation renovationTaskValidation;

    @BeforeAll
    static void validatorSetup() {
        renovationTaskValidation = new RenovationTaskValidation();
    }


    @Test
    void testValidateName_validName_noError() {
        String result = renovationTaskValidation.validateName("Kitchen Renovation", "Task");
        assertNull(result);
    }

    @Test
    void testValidateName_invalidName_error() {
        String result = renovationTaskValidation.validateName("Kitchen@Renovation", "Task");
        assertNotNull(result);
        assertTrue(result.contains("Task name cannot be empty and must only include"));
    }

    @Test
    void testValidateDescription_validDescription_noError() {
        String result = renovationTaskValidation.validateDescription("Fixing cabinets", "Task");
        assertNull(result);
    }

    @Test
    void testValidateDescription_emptyDescription_error() {
        String result = renovationTaskValidation.validateDescription("   ", "Task");
        assertNotNull(result);
        assertTrue(result.contains("Task description cannot be empty."));
    }

    @Test
    void testValidateDescription_tooLongDescription_error() {
        String longDesc = "a".repeat(513);
        String result = renovationTaskValidation.validateDescription(longDesc, "Task");
        assertNotNull(result);
        assertTrue(result.contains("Task description must be 512 characters or less."));
    }

    @Test
    void testValidateDueDate_pastDate_error() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String result = renovationTaskValidation.validateDueDate(yesterday);
        assertEquals("Due date must be in the future.", result);
    }

    @Test
    void testValidateDueDate_futureDate_noError() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String result = renovationTaskValidation.validateDueDate(tomorrow);
        assertNull(result);
    }

    @Test
    void testValidateDueDate_nullDate_noError() {
        String result = renovationTaskValidation.validateDueDate(null);
        assertNull(result);
    }
}

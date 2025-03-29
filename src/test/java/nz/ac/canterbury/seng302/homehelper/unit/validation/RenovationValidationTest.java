package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RenovationValidationTest {

    private static RenovationValidation renovationValidation;

    @BeforeAll
    static void validatorSetup() {
        renovationValidation = new RenovationValidation();
    }

    /**
     * Test for checking if no error messages are returned if the all the details are valid. Boundaries are tested as the name
     * has special letters (ā, Ö, é) and the description has length equal to the maximum accepted length.
     */
    @Test
    public void validateTaskDetails_allDetailsAreValid_returnEmptyList() {


        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Tāsk Öné 2-3", "A".repeat(512), LocalDate.now().plusDays(1));

        List<String> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        System.out.println(errors);
        assertTrue(errors.isEmpty());
    }

    /**
     * Test for checking if an error message is returned if the name is empty (has only spaces).
     */
    @Test
    public void validateTaskDetails_nameOnlyHasSpaces_returnNameFormatError() {

        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("  ", "Some description", LocalDate.now().plusDays(1));

        List<String> expectedErrors = List.of("Task name cannot be empty and must only include letters, numbers, " +
                "spaces, dots, hyphens or apostrophes.");
        List<String> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertIterableEquals(expectedErrors, errors);
    }

    /**
     * Test for checking if an error message is returned if the name has invalid characters.
     */
    @Test
    public void validateTaskDetails_nameHasInvalidCharacters_returnNameFormatError() {

        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One!", "Some description", LocalDate.now().plusDays(1));

        List<String> expectedErrors = List.of("Task name cannot be empty and must only include letters, numbers, " +
                "spaces, dots, hyphens or apostrophes.");
        List<String> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertIterableEquals(expectedErrors, errors);
    }

    /**
     * Test for checking if an error message is returned if the description is empty (has only spaces).
     */
    @Test
    public void validateTaskDetails_descriptionOnlyHasSpaces_returnDescriptionEmptyList() {

        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "  ", LocalDate.now().plusDays(1));

        List<String> expectedErrors = List.of("Task description cannot be empty.");
        List<String> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertIterableEquals(expectedErrors, errors);
    }

    /**
     * Test for checking if an error message is returned if the description is too long.
     */
    @Test
    public void validateTaskDetails_descriptionIsTooLong_returnDescriptionLengthError() {

        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "A".repeat(513), LocalDate.now().plusDays(1));

        List<String> expectedErrors = List.of("Task description must be 512 characters or less.");
        List<String> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertIterableEquals(expectedErrors, errors);
    }

    /**
     * Test for checking if an error message is returned if the date is in the past.
     */
    @Test
    public void validateTaskDetails_dueDateInPast_returnInvalidDueDateError() {

        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "Some description", LocalDate.now().minusDays(1));

        List<String> expectedErrors = List.of("Due date must be in the future.");
        List<String> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertIterableEquals(expectedErrors, errors);
    }
}

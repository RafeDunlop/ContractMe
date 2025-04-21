package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RenovationValidationTest {

    private static RenovationValidation renovationValidation;

    @BeforeAll
    static void validatorSetup() {
        renovationValidation = new RenovationValidation();
    }

    @Test
    public void validateTaskDetails_allDetailsAreValid_returnEmptyMap() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Tāsk Öné 2-3", "A".repeat(512), LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void validateTaskDetails_nameOnlyHasSpaces_returnNameFormatError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("  ", "Some description", LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("nameError", List.of("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes."));

        Map<String, List<String>> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_nameHasInvalidCharacters_returnNameFormatError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One!", "Some description", LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("nameError", List.of("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes."));

        Map<String, List<String>> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_descriptionOnlyHasSpaces_returnDescriptionEmptyError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "  ", LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("descriptionError", List.of("Task description cannot be empty."));

        Map<String, List<String>> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_descriptionIsTooLong_returnDescriptionLengthError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "A".repeat(513), LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("descriptionError", List.of("Task description must be 512 characters or less."));

        Map<String, List<String>> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_dueDateInPast_returnInvalidDueDateError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "Some description", LocalDate.now().minusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("dueDateError", List.of("Due date must be in the future."));

        Map<String, List<String>> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);
        assertEquals(expectedErrors, errors);
    }
}
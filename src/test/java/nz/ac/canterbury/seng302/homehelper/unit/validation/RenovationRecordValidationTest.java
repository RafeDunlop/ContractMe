package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationRecordValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RenovationRecordValidationTest {
    private static RenovationRecordValidation toTest;
    private static RenovationRecord existingRenovation;

    @BeforeAll
    public static void setUpBeforeClass() {
        RenovationRecordRepository repository = Mockito.mock(RenovationRecordRepository.class);
        User user = Mockito.mock(User.class);
        LoginService loginService = Mockito.mock(LoginService.class);
        existingRenovation = Mockito.mock(RenovationRecord.class);
        Mockito.when(existingRenovation.getName()).thenReturn("already exists");
        Mockito.when(existingRenovation.getId()).thenReturn(1L);

        Mockito.when(loginService.getUserByEmail()).thenReturn(user);
        Mockito.when(repository.findExactMatch("already exists", user)).thenReturn(Optional.of(existingRenovation));
        Mockito.when(repository.findExactMatch("name", user)).thenReturn(Optional.empty());
        Mockito.when(repository.findExactMatch("name!", user)).thenReturn(Optional.empty());

        toTest = new RenovationRecordValidation(repository, loginService);
    }

    @Test
    public void checkForExactMatchCreate_noMatch() {
        List<String> errors = toTest.checkForExactMatchCreate("name");
        assertTrue(errors.isEmpty(), "Expected no errors when the renovation exists.");
    }

    @Test
    void testValidateName_maxLength255_noError() {
        String name128 = "a".repeat(128);
        List<String> result = toTest.validateName(name128);
        assertTrue(result.isEmpty());
    }

    @Test
    void testValidateName_maxLength255_Error() {
        String name128 = "a".repeat(129);
        List<String> result = toTest.validateName(name128);
        assertEquals(" Name cannot be greater than 128 characters.",result.get(0));
    }
    @Test
    public void checkForExactMatchCreate_match() {
        List<String> errors = toTest.checkForExactMatchCreate("already exists");
        assertEquals(1, errors.size());
        assertEquals("A renovation with this name already exists.", errors.get(0));
    }

    @Test
    public void checkForExactMatchEdit_noChange() {
        RenovationRecord renovation = new RenovationRecord();
        renovation.setName("name");
        List<String> errors = toTest.checkForExactMatchEdit("name", renovation);
        assertTrue(errors.isEmpty(), "Expected no errors when name unchanged and no duplicate.");
    }

    @Test
    public void checkForExactMatchEdit_nameChanged() {
        RenovationRecord renovation = Mockito.mock(RenovationRecord.class);
        Mockito.when(renovation.getName()).thenReturn("original name");
        Mockito.when(renovation.getId()).thenReturn(2L);
        List<String> errors = toTest.checkForExactMatchEdit("already exists", renovation);
        assertEquals(1, errors.size());
        assertEquals("A renovation with this name already exists.", errors.get(0));
    }

    @Test
    public void checkForExactMatchCreate_sameNameDifferentUser_allowed() {
        User userA = Mockito.mock(User.class);
        User userB = Mockito.mock(User.class);

        RenovationRecordRepository repo = Mockito.mock(RenovationRecordRepository.class);
        LoginService login = Mockito.mock(LoginService.class);

        Mockito.when(repo.findExactMatch("duplicate name", userA)).thenReturn(Optional.of(existingRenovation));
        Mockito.when(repo.findExactMatch("duplicate name", userB)).thenReturn(Optional.empty());

        Mockito.when(login.getUserByEmail()).thenReturn(userB);
        RenovationRecordValidation validator = new RenovationRecordValidation(repo, login);

        List<String> errors = validator.checkForExactMatchCreate("duplicate name");

        assertTrue(errors.isEmpty());
    }


    @Test
    public void validateDescription_validShort() {
        String description = "Short description.";
        List<String> errors = toTest.validateDescription(description);
        assertTrue(errors.isEmpty(), "Expected no errors for short valid description.");
    }

    @Test
    public void validateDescription_exactLimit() {
        String description = "a".repeat(512);
        List<String> errors = toTest.validateDescription(description);
        assertTrue(errors.isEmpty(), "Expected no errors for description exactly 512 characters.");
    }

    @Test
    public void validateDescription_tooLong() {
        String description = "a".repeat(513);
        List<String> errors = toTest.validateDescription(description);
        assertEquals(1, errors.size());
        assertEquals("Renovation record description must be 512 characters or less.", errors.get(0));
    }

    @Test
    public void validateRooms_emptyList() {
        List<String> rooms = new ArrayList<>();
        List<String> errors = toTest.validateRooms(rooms);
        assertTrue(errors.isEmpty(), "Expected no errors for empty room list.");
    }

    @Test
    public void validateRooms_validRooms() {
        List<String> rooms = List.of("Kitchen", "Living Room", "Bedroom-1");
        List<String> errors = toTest.validateRooms(rooms);
        assertTrue(errors.isEmpty(), "Expected no errors for valid room names.");
    }

    @Test
    public void validateRooms_invalidRoom() {
        List<String> rooms = List.of("Kitchen", "Living@Room");
        List<String> errors = toTest.validateRooms(rooms);
        assertEquals(1, errors.size());
        assertEquals("Room names must only contain letters, numbers, spaces, commas, dots, hyphens, or apostrophes.", errors.get(0));
    }

    @Test
    public void validateName_empty() {
        String name = "";
        List<String> errors = toTest.validateName(name);
        assertTrue(errors.contains("Renovation record name cannot be empty."));
    }

    @Test
    public void validateName_validSimple() {
        String name = "Kitchen Renovation";
        List<String> errors = toTest.validateName(name);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void validateName_validUnicode() {
        String name = "Rénovation Étage-1, Suite.";
        List<String> errors = toTest.validateName(name);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void validateName_invalidCharacters() {
        String name = "Invalid@Name!";
        List<String> errors = toTest.validateName(name);
        assertTrue(errors.contains("Renovation record room names must only include letters, numbers, spaces, dots, hyphens or apostrophes."));
    }

    @Test
    public void validateName_onlyInvalidCharacters() {
        String name = "@#$%^&*";
        List<String> errors = toTest.validateName(name);
        assertTrue(errors.contains("Renovation record room names must only include letters, numbers, spaces, dots, hyphens or apostrophes."));
    }
}

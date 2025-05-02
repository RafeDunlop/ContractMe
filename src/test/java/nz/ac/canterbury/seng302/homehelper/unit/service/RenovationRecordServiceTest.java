package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationRecordValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RenovationRecordServiceTest {

    private static RenovationRecordService toTest;

    private static RenovationRecordValidation renovationRecordValidation;
    private static RenovationRecordRepository renovationRecordRepository;
    private static LoginService loginService;

    @BeforeAll
    public static void setUpBeforeClass() {
        renovationRecordRepository = Mockito.mock(RenovationRecordRepository.class);
        loginService = Mockito.mock(LoginService.class);
        renovationRecordValidation = new RenovationRecordValidation(renovationRecordRepository, loginService);

        User mockUser = Mockito.mock(User.class);
        Mockito.when(loginService.getUserByEmail()).thenReturn(mockUser);

        Mockito.when(loginService.getUserByEmail()).thenReturn(mockUser);
        Mockito.when(renovationRecordRepository.findExactMatch("already exists", mockUser)).thenReturn(Optional.of(Mockito.mock(RenovationRecord.class)));
        Mockito.when(renovationRecordRepository.findExactMatch("name", mockUser)).thenReturn(Optional.empty());
        Mockito.when(renovationRecordRepository.findExactMatch("name!", mockUser)).thenReturn(Optional.empty());

        toTest = new RenovationRecordService(renovationRecordRepository, renovationRecordValidation);
    }

    @Test
    public void validateAllCreate_allValid() {
        List<String> rooms = new ArrayList<>();
        String name = "name";
        String description = "";

        Map<String, List<String>> result = toTest.validateAllInputsCreate(name, description, rooms);

        assertTrue(result.isEmpty(), "Expected no validation errors, but got: " + result);
    }

    @Test
    public void validateAllCreate_oneInvalid() {
        List<String> rooms = new ArrayList<>();
        String name = "name!";
        String description = "";

        Map<String, List<String>> result = toTest.validateAllInputsCreate(name, description, rooms);

        assertFalse(result.isEmpty(), "Expected validation errors, but got none.");
        assertTrue(result.containsKey("nameError"), "Expected an error for the 'name' field.");
        assertFalse(result.get("nameError").isEmpty(), "Expected at least one error message for the 'name' field.");
    }


    @Test
    public void validateAllEdit_allValid() {
        ArrayList<String> rooms = new ArrayList<>();
        String name = "name";
        String description = "";
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);
        Mockito.when(renovationRecord.getName()).thenReturn(name);
        Mockito.when(renovationRecord.getDescription()).thenReturn(description);
        Mockito.when(renovationRecord.getRooms()).thenReturn(rooms);
        assertTrue(toTest.validateAllInputsEdit(renovationRecord, "differentName").isEmpty());
    }

    @Test
    public void validateAllEdit_oneInvalid() {
        ArrayList<String> rooms = new ArrayList<>();
        String name = "name";
        String description = "";
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);
        Mockito.when(renovationRecord.getName()).thenReturn(name);
        Mockito.when(renovationRecord.getDescription()).thenReturn(description);
        Mockito.when(renovationRecord.getRooms()).thenReturn(rooms);
        assertTrue(toTest.validateAllInputsEdit(renovationRecord, "already exists").isEmpty());
    }

    @Test
    public void validateAllEdit_sameName() {
        ArrayList<String> rooms = new ArrayList<>();
        String name = "already exists";
        String description = "";
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);
        Mockito.when(renovationRecord.getName()).thenReturn(name);
        Mockito.when(renovationRecord.getDescription()).thenReturn(description);
        Mockito.when(renovationRecord.getRooms()).thenReturn(rooms);
        assertTrue(toTest.validateAllInputsEdit(renovationRecord, "already exists").isEmpty());
    }

    @Test
    public void validateAllEdit_sameNameOtherProblem() {
        ArrayList<String> rooms = new ArrayList<>();
        String name = "already exists";
        String description = "a".repeat(513);
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);
        Mockito.when(renovationRecord.getName()).thenReturn(name);
        Mockito.when(renovationRecord.getDescription()).thenReturn(description);
        Mockito.when(renovationRecord.getRooms()).thenReturn(rooms);
        assertFalse(toTest.validateAllInputsEdit(renovationRecord, "already exists").isEmpty());
    }

    @Test
    public void validateAllInputsCreate_sameName_differentUsers() {
        User userA = Mockito.mock(User.class);
        User userB = Mockito.mock(User.class);

        String renovationName = "Renovation A";

        RenovationRecord existingRenovation = Mockito.mock(RenovationRecord.class);
        Mockito.when(existingRenovation.getName()).thenReturn(renovationName);

        Mockito.when(loginService.getUserByEmail()).thenReturn(userA);
        Mockito.when(renovationRecordRepository.findExactMatch(renovationName, userA)).thenReturn(Optional.of(existingRenovation));

        Mockito.when(loginService.getUserByEmail()).thenReturn(userB);
        Mockito.when(renovationRecordRepository.findExactMatch(renovationName, userB)).thenReturn(Optional.empty());

        Map<String, List<String>> errorsForUserA = toTest.validateAllInputsCreate(renovationName, "Some description", Arrays.asList("Kitchen", "Living Room"));
        assertTrue(errorsForUserA.isEmpty(), "User A should not have errors when creating a renovation with an existing name.");

        Map<String, List<String>> errorsForUserB = toTest.validateAllInputsCreate(renovationName, "Another description", Arrays.asList("Bedroom"));
        assertTrue(errorsForUserB.isEmpty(), "User B should be able to create a renovation with the same name.");
    }

    @Test
    public void validateAllInputsCreate_nameConflictSameUser() {
        User userA = Mockito.mock(User.class);

        String renovationName = "Renovation A";

        RenovationRecord existingRenovation = Mockito.mock(RenovationRecord.class);
        Mockito.when(existingRenovation.getName()).thenReturn(renovationName);

        Mockito.when(loginService.getUserByEmail()).thenReturn(userA);
        Mockito.when(renovationRecordRepository.findExactMatch(renovationName, userA)).thenReturn(Optional.of(existingRenovation));

        Map<String, List<String>> errors = toTest.validateAllInputsCreate(renovationName, "Some description", Arrays.asList("Kitchen", "Living Room"));
        assertFalse(errors.isEmpty(), "User A should get an error when trying to create a renovation with the same name.");
        assertTrue(errors.get("nameError").contains("A renovation with this name already exists."));
    }

    @Test
    public void validateAllInputsCreate_noConflict() {
        User userA = Mockito.mock(User.class);

        String renovationName = "Renovation B";

        Mockito.when(loginService.getUserByEmail()).thenReturn(userA);
        Mockito.when(renovationRecordRepository.findExactMatch(renovationName, userA)).thenReturn(Optional.empty());

        Map<String, List<String>> errors = toTest.validateAllInputsCreate(renovationName, "Some description", Arrays.asList("Kitchen"));
        assertTrue(errors.isEmpty(), "User A should not have any errors when creating a new renovation.");
    }
}

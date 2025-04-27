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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RenovationRecordServiceTest {

    private static RenovationRecordService toTest;

    @BeforeAll
    public static void setUpBeforeClass() {
        RenovationRecordRepository repository = Mockito.mock(RenovationRecordRepository.class);
        User user = Mockito.mock(User.class);
        LoginService loginService = Mockito.mock(LoginService.class);
        RenovationRecordValidation renovationRecordValidation = new RenovationRecordValidation(repository, loginService);

        Mockito.when(loginService.getUserByEmail()).thenReturn(user);
        Mockito.when(repository.findExactMatch("already exists", user)).thenReturn(Optional.of(Mockito.mock(RenovationRecord.class)));
        Mockito.when(repository.findExactMatch("name", user)).thenReturn(Optional.empty());
        Mockito.when(repository.findExactMatch("name!", user)).thenReturn(Optional.empty());

        toTest = new RenovationRecordService(repository, renovationRecordValidation);
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
}

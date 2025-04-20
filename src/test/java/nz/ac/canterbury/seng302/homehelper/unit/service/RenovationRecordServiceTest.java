package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RenovationRecordServiceTest {

    private static RenovationRecordService toTest;

    private static final Pattern pattern = Pattern.compile("^[\\p{L}\\d ,.\\-']+$", Pattern.UNICODE_CHARACTER_CLASS);

    @BeforeAll
    public static void setUpBeforeClass() {
        RenovationRecordRepository repository = Mockito.mock(RenovationRecordRepository.class);
        Mockito.when(repository.findExactMatch("already exists")).thenReturn(Optional.of(Mockito.mock(RenovationRecord.class)));
        Mockito.when(repository.findExactMatch("name")).thenReturn(Optional.empty());
        Mockito.when(repository.findExactMatch("name!")).thenReturn(Optional.empty());
        toTest = new RenovationRecordService(repository);
    }

    @Test
    public void checkForExactMatch_noMatch() {
        assertFalse(toTest.checkForExactMatch("name"));
    }

    @Test
    public void checkForExactMatch_match() {
        assertTrue(toTest.checkForExactMatch("already exists"));
    }

    @Test
    public void validateDescriptionLength_empty() {
        String description = "";
        assertTrue(toTest.validateDescriptionLength(description));
    }

    @Test
    public void validateDescriptionLength_short() {
        String description = "too expensive";
        assertTrue(toTest.validateDescriptionLength(description));
    }

    @Test
    public void validateDescriptionLength_edge() {
        String description = "a".repeat(512);
        assertTrue(toTest.validateDescriptionLength(description));
    }

    @Test
    public void validateDescriptionLength_tooLong() {
        String description = "a".repeat(513);
        assertFalse(toTest.validateDescriptionLength(description));
    }

    @Test
    public void validateAllRoomNames_empty() {
        ArrayList<String> rooms = new ArrayList<>();
        assertTrue(toTest.validateAllRoomNames(rooms, pattern));
    }

    @Test
    public void validateAllRoomNames_onePassing() {
        ArrayList<String> rooms = new ArrayList<>();
        rooms.add("a");
        assertTrue(toTest.validateAllRoomNames(rooms, pattern));
    }

    @Test
    public void validateAllRoomNames_multiPassing() {
        ArrayList<String> rooms = new ArrayList<>();
        for (int i=1; i<10; i++) rooms.add("a".repeat(i));
        assertTrue(toTest.validateAllRoomNames(rooms, pattern));
    }

    @Test
    public void validateAllRoomNames_multiOneFailing() {
        ArrayList<String> rooms = new ArrayList<>();
        rooms.add("%");
        for (int i=0; i<10; i++) rooms.add("a".repeat(i));
        assertFalse(toTest.validateAllRoomNames(rooms, pattern));
    }
    
    @Test
    public void validateName_empty() {
        String name = "";
        assertFalse(toTest.validateName(name, pattern));
    }

    @Test
    public void validateName_pass() {
        String name = "name";
        assertTrue(toTest.validateName(name, pattern));
    }

    @Test
    public void validateName_unicodePass() {
        String name ="éòçñ ,'-012345679AbCd";
        assertTrue(toTest.validateName(name, pattern));
    }

    @Test
    public void validateName_oneFail() {
        String name = "name!";
        assertFalse(toTest.validateName(name, pattern));
    }

    @Test
    public void validateName_allFail() {
        String name = "@#$%^";
        assertFalse(toTest.validateName(name, pattern));
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
        assertFalse(toTest.validateAllInputsEdit(renovationRecord, "already exists").isEmpty());
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

package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationRecordValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RenovationRecordServiceTest {

    private static RenovationRecordService toTest;

    private static RenovationRecordValidation renovationRecordValidation;
    private static RenovationRecordRepository renovationRecordRepository;
    private static RenovationTaskRepository renovationTaskRepository;
    private static LoginService loginService;

    @BeforeAll
    public static void setUpBeforeClass() {
        renovationRecordRepository = mock(RenovationRecordRepository.class);
        renovationTaskRepository = mock(RenovationTaskRepository.class);
        loginService = mock(LoginService.class);
        renovationRecordValidation = new RenovationRecordValidation(renovationRecordRepository, loginService);

        User mockUser = mock(User.class);
        Mockito.when(loginService.getUserByEmail()).thenReturn(mockUser);

        Mockito.when(loginService.getUserByEmail()).thenReturn(mockUser);
        Mockito.when(renovationRecordRepository.findExactMatch("already exists", mockUser)).thenReturn(Optional.of(
                mock(RenovationRecord.class)));
        Mockito.when(renovationRecordRepository.findExactMatch("name", mockUser)).thenReturn(Optional.empty());
        Mockito.when(renovationRecordRepository.findExactMatch("name!", mockUser)).thenReturn(Optional.empty());

        toTest = new RenovationRecordService(renovationRecordRepository, renovationTaskRepository, renovationRecordValidation);
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
    public void testAddRenovationLocation_locationAdded() {

        User mockUser = mock(User.class);


        RenovationRecord renovationRecord = new RenovationRecord(mockUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1("123 Main St");
        addressDTO.setCountry("New Zealand");
        addressDTO.setPostcode("8011");
        addressDTO.setCity("Christchurch");
        addressDTO.setRegion("Canterbury");

        toTest.addRenovationLocation(renovationRecord, addressDTO);

        ArgumentCaptor<RenovationRecord> captor = ArgumentCaptor.forClass(RenovationRecord.class);
        Mockito.verify(renovationRecordRepository, Mockito.times(2)).save(captor.capture());


        RenovationRecord savedRenovation = captor.getValue();
        Location location = savedRenovation.getLocation();

        assertNotNull(location);
        assertEquals("123 Main St", location.getAddress());
        assertEquals("New Zealand", location.getCountry());
        assertEquals("8011", location.getPostcode());
        assertEquals("Christchurch", location.getCity());
        assertEquals("Canterbury", location.getSuburb());
    }

    @Test
    public void setRenovationPublic_isPublic() {

        User mockUser = mock(User.class);

        RenovationRecord renovationRecord = new RenovationRecord(mockUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));


        toTest.changePublicity(true, renovationRecord);

        assertTrue(renovationRecord.isPublic(), "Publicity flag should be set to true");
    }

    @Test
    public void setRenovationNotPublic_isNotPublic() {

        User mockUser = mock(User.class);

        RenovationRecord renovationRecord = new RenovationRecord(mockUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));

        toTest.changePublicity(false, renovationRecord);

        assertFalse(renovationRecord.isPublic(), "Publicity flag should be set to true");
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
        RenovationRecord renovationRecord = mock(RenovationRecord.class);
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
        RenovationRecord renovationRecord = mock(RenovationRecord.class);
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
        RenovationRecord renovationRecord = mock(RenovationRecord.class);
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
        RenovationRecord renovationRecord = mock(RenovationRecord.class);
        Mockito.when(renovationRecord.getName()).thenReturn(name);
        Mockito.when(renovationRecord.getDescription()).thenReturn(description);
        Mockito.when(renovationRecord.getRooms()).thenReturn(rooms);
        assertFalse(toTest.validateAllInputsEdit(renovationRecord, "already exists").isEmpty());
    }

    @Test
    public void validateAllInputsCreate_sameName_differentUsers() {
        User userA = mock(User.class);
        User userB = mock(User.class);

        String renovationName = "Renovation A";

        RenovationRecord existingRenovation = mock(RenovationRecord.class);
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
        User userA = mock(User.class);

        String renovationName = "Renovation A";

        RenovationRecord existingRenovation = mock(RenovationRecord.class);
        Mockito.when(existingRenovation.getName()).thenReturn(renovationName);

        Mockito.when(loginService.getUserByEmail()).thenReturn(userA);
        Mockito.when(renovationRecordRepository.findExactMatch(renovationName, userA)).thenReturn(Optional.of(existingRenovation));

        Map<String, List<String>> errors = toTest.validateAllInputsCreate(renovationName, "Some description", Arrays.asList("Kitchen", "Living Room"));
        assertFalse(errors.isEmpty(), "User A should get an error when trying to create a renovation with the same name.");
        assertTrue(errors.get("nameError").contains("A renovation with this name already exists."));
    }

    @Test
    public void validateAllInputsCreate_noConflict() {
        User userA = mock(User.class);

        String renovationName = "Renovation B";

        Mockito.when(loginService.getUserByEmail()).thenReturn(userA);
        Mockito.when(renovationRecordRepository.findExactMatch(renovationName, userA)).thenReturn(Optional.empty());

        Map<String, List<String>> errors = toTest.validateAllInputsCreate(renovationName, "Some description", Arrays.asList("Kitchen"));
        assertTrue(errors.isEmpty(), "User A should not have any errors when creating a new renovation.");
    }

    @Test
    public void getUserRecords_withNullTerm_returnsAllUserRecords() {
        User user = mock(User.class);
        List<RenovationRecord> expected = List.of(mock(RenovationRecord.class));

        Mockito.when(renovationRecordRepository.findByUser(user)).thenReturn(expected);

        List<RenovationRecord> result = toTest.getUserRecords(user, null);

        verify(renovationRecordRepository).findByUser(user);
        assertSame(result, expected);
    }

    @Test
    public void getUserRecords_withSearchTerm_returnsFilteredRecords() {
        User user = mock(User.class);
        String term = "kitchen";
        List<RenovationRecord> expected = List.of(mock(RenovationRecord.class));

        Mockito.when(renovationRecordRepository.findByUserTrueSearchContainingNameOrDescriptionIgnoreCase(user, term)).thenReturn(expected);

        List<RenovationRecord> result = toTest.getUserRecords(user, term);

        verify(renovationRecordRepository).findByUserTrueSearchContainingNameOrDescriptionIgnoreCase(user, term);
        assertSame(result, expected);
    }

    @Test
    public void getPublicRecords_withNullTerm_returnsAllPublicRecords() {
        List<RenovationRecord> expected = List.of(mock(RenovationRecord.class));

        Mockito.when(renovationRecordRepository.findByIsPublicTrue()).thenReturn(expected);

        List<RenovationRecord> result = toTest.getPublicRecords(null);

        verify(renovationRecordRepository).findByIsPublicTrue();
        assertSame(result, expected);
    }

    @Test
    public void getPublicRecords_withSearchTerm_returnsFilteredPublicRecords() {
        String term = "bathroom";
        List<RenovationRecord> expected = List.of(mock(RenovationRecord.class));

        Mockito.when(renovationRecordRepository.findByIsPublicTrueSearchContainingNameOrDescriptionIgnoreCase(term)).thenReturn(expected);

        List<RenovationRecord> result = toTest.getPublicRecords(term);

        verify(renovationRecordRepository).findByIsPublicTrueSearchContainingNameOrDescriptionIgnoreCase(term);
        assertSame(result, expected);
    }

    @Test
    public void getAllRecords_withNullTerm_returnsAllVisibleToUser() {
        User user = mock(User.class);
        List<RenovationRecord> expected = List.of(mock(RenovationRecord.class));

        Mockito.when(renovationRecordRepository.findAllVisibleToUser(user)).thenReturn(expected);

        List<RenovationRecord> result = toTest.getAllRecords(user, null);

        verify(renovationRecordRepository).findAllVisibleToUser(user);
        assertSame(result, expected);
    }

    @Test
    public void getAllRecords_withSearchTerm_returnsFilteredRecords() {
        User user = mock(User.class);
        String term = "garage";
        List<RenovationRecord> expected = List.of(mock(RenovationRecord.class));

        Mockito.when(renovationRecordRepository.findAllVisibleToUserSearchContainingNameOrDescriptionIgnoreCase(user, term)).thenReturn(expected);

        List<RenovationRecord> result = toTest.getAllRecords(user, term);

        verify(renovationRecordRepository).findAllVisibleToUserSearchContainingNameOrDescriptionIgnoreCase(user, term);
        assertSame(result, expected);
    }

    @Test
    public void getPaginatedUserRecords_withNullTerm_callsFindByUserMethod() {
        User user = mock(User.class);
        String term = null;


        Pageable pageable = PageRequest.of(0, 10);
        toTest.getPaginatedUserRecords(user, term, pageable);

        verify(renovationRecordRepository).findByUser(user, pageable);
    }

    @Test
    public void getPaginatedUserRecords_withSearchTerm_callsSearchMethod() {
        User user = mock(User.class);
        String term = "living room";

        Pageable pageable = PageRequest.of(0, 10);
        toTest.getPaginatedUserRecords(user, term, pageable);

        verify(renovationRecordRepository).searchNameOrDescriptionContainingIgnoreCasePaginated(user, term, pageable);
    }

    @Test
    void returnRecordPages_withNullList_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<RenovationRecord> result = toTest.returnRecordPages(pageable, null);

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void returnRecordPages_withEmptyList_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<RenovationRecord> result = toTest.returnRecordPages(pageable, new ArrayList<>());

        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void returnRecordPages_withValidPageable_returnsCorrectSublist() {
        List<RenovationRecord> allRecords = createRecords(10);
        Pageable pageable = PageRequest.of(1, 5);
        Page<RenovationRecord> result = toTest.returnRecordPages(pageable, allRecords);

        assertEquals(5, result.getContent().size());
        assertEquals("Record 5", result.getContent().get(0).getName());
        assertEquals("Record 9", result.getContent().get(4).getName());
        assertEquals(10, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void returnRecordPages_withPartialLastPage_returnsRemainingRecords() {
        List<RenovationRecord> allRecords = createRecords(7);
        Pageable pageable = PageRequest.of(1, 5);
        Page<RenovationRecord> result = toTest.returnRecordPages(pageable, allRecords);

        assertEquals(2, result.getContent().size());
        assertEquals("Record 5", result.getContent().get(0).getName());
        assertEquals("Record 6", result.getContent().get(1).getName());
        assertEquals(2, result.getTotalPages());
    }

    private List<RenovationRecord> createRecords(int count) {
        List<RenovationRecord> records = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            records.add(new RenovationRecord(mock(User.class), "Record " + i, "", Collections.emptyList()));
        }
        return records;
    }
}

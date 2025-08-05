package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.CalendarCellDTO;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationRecordDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationRecordValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RenovationRecordServiceTest {

    private static RenovationRecordService toTest;

    private static RenovationRecordValidation renovationRecordValidation;
    private static RenovationRecordRepository renovationRecordRepository;
    private static RenovationTaskRepository renovationTaskRepository;
    private static RenovationTaskService renovationTaskService;
    private static LocationService locationService;
    private static LoginService loginService;
    private static RenovationRecord mockRenovationRecord;

    @BeforeAll
    public static void setUpBeforeClass() {
        renovationRecordRepository = mock(RenovationRecordRepository.class);
        renovationTaskRepository = mock(RenovationTaskRepository.class);
        loginService = mock(LoginService.class);
        renovationRecordValidation = new RenovationRecordValidation(renovationRecordRepository, loginService);
        renovationTaskService = mock(RenovationTaskService.class);
        locationService = mock(LocationService.class);


        User mockUser = mock(User.class);
        when(loginService.getUserByEmail()).thenReturn(mockUser);

        when(loginService.getUserByEmail()).thenReturn(mockUser);
        when(renovationRecordRepository.findExactMatch("already exists", mockUser)).thenReturn(Optional.of(
                mock(RenovationRecord.class)));
        when(renovationRecordRepository.findExactMatch("name", mockUser)).thenReturn(Optional.empty());
        when(renovationRecordRepository.findExactMatch("name!", mockUser)).thenReturn(Optional.empty());

        toTest = new RenovationRecordService(renovationRecordRepository, renovationTaskRepository,
                renovationRecordValidation, renovationTaskService, locationService);

        mockRenovationRecord = mock(RenovationRecord.class);
        when(renovationTaskService.getTasksWithinDates(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new HashMap<>());
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

        when(locationService.locate(addressDTO)).thenReturn(new Location(
                addressDTO.getAddress_line1(),
                addressDTO.getCountry(),
                addressDTO.getPostcode(),
                addressDTO.getCity(),
                addressDTO.getRegion()
        ));

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
        when(renovationRecord.getName()).thenReturn(name);
        when(renovationRecord.getDescription()).thenReturn(description);
        when(renovationRecord.getRooms()).thenReturn(rooms);
        assertTrue(toTest.validateAllInputsEdit(renovationRecord, "differentName").isEmpty());
    }

    @Test
    public void validateAllEdit_oneInvalid() {
        ArrayList<String> rooms = new ArrayList<>();
        String name = "name";
        String description = "";
        RenovationRecord renovationRecord = mock(RenovationRecord.class);
        when(renovationRecord.getName()).thenReturn(name);
        when(renovationRecord.getDescription()).thenReturn(description);
        when(renovationRecord.getRooms()).thenReturn(rooms);
        assertTrue(toTest.validateAllInputsEdit(renovationRecord, "already exists").isEmpty());
    }

    @Test
    public void validateAllEdit_sameName() {
        ArrayList<String> rooms = new ArrayList<>();
        String name = "already exists";
        String description = "";
        RenovationRecord renovationRecord = mock(RenovationRecord.class);
        when(renovationRecord.getName()).thenReturn(name);
        when(renovationRecord.getDescription()).thenReturn(description);
        when(renovationRecord.getRooms()).thenReturn(rooms);
        assertTrue(toTest.validateAllInputsEdit(renovationRecord, "already exists").isEmpty());
    }

    @Test
    public void validateAllEdit_sameNameOtherProblem() {
        ArrayList<String> rooms = new ArrayList<>();
        String name = "already exists";
        String description = "a".repeat(513);
        RenovationRecord renovationRecord = mock(RenovationRecord.class);
        when(renovationRecord.getName()).thenReturn(name);
        when(renovationRecord.getDescription()).thenReturn(description);
        when(renovationRecord.getRooms()).thenReturn(rooms);
        assertFalse(toTest.validateAllInputsEdit(renovationRecord, "already exists").isEmpty());
    }

    @Test
    public void validateAllInputsCreate_sameName_differentUsers() {
        User userA = mock(User.class);
        User userB = mock(User.class);

        String renovationName = "Renovation A";

        RenovationRecord existingRenovation = mock(RenovationRecord.class);
        when(existingRenovation.getName()).thenReturn(renovationName);

        when(loginService.getUserByEmail()).thenReturn(userA);
        when(renovationRecordRepository.findExactMatch(renovationName, userA)).thenReturn(Optional.of(existingRenovation));

        when(loginService.getUserByEmail()).thenReturn(userB);
        when(renovationRecordRepository.findExactMatch(renovationName, userB)).thenReturn(Optional.empty());

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
        when(existingRenovation.getName()).thenReturn(renovationName);

        when(loginService.getUserByEmail()).thenReturn(userA);
        when(renovationRecordRepository.findExactMatch(renovationName, userA)).thenReturn(Optional.of(existingRenovation));

        Map<String, List<String>> errors = toTest.validateAllInputsCreate(renovationName, "Some description", Arrays.asList("Kitchen", "Living Room"));
        assertFalse(errors.isEmpty(), "User A should get an error when trying to create a renovation with the same name.");
        assertTrue(errors.get("nameError").contains("A renovation with this name already exists."));
    }

    @Test
    public void validateAllInputsCreate_noConflict() {
        User userA = mock(User.class);

        String renovationName = "Renovation B";

        when(loginService.getUserByEmail()).thenReturn(userA);
        when(renovationRecordRepository.findExactMatch(renovationName, userA)).thenReturn(Optional.empty());

        Map<String, List<String>> errors = toTest.validateAllInputsCreate(renovationName, "Some description", Arrays.asList("Kitchen"));
        assertTrue(errors.isEmpty(), "User A should not have any errors when creating a new renovation.");
    }

    @Test
    public void getUserRecords_withNullTerm_returnsAllUserRecords() {
        User user = new User("test@example.com", "pass", "Test", "User");
        RenovationRecord record = new RenovationRecord(user, "Test Renovation", "Test Desc", List.of());
        record.setPublicity(true);
        record.setCreatedTimestamp(LocalDateTime.now());

        List<RenovationRecord> records = List.of(record);
        Page<RenovationRecord> mockPage = mock(Page.class);
        Pageable pageable = PageRequest.of(0, 10);

        when(mockPage.getContent()).thenReturn(records);
        when(renovationRecordRepository.findUserRecords(eq(user), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<RenovationRecordDTO> result = toTest.getPaginatedUserRecords(user, "", null, pageable);

        verify(renovationRecordRepository).findUserRecords(eq(user), any(Pageable.class));
        assertEquals(1, result.getContent().size());

        RenovationRecordDTO dto = result.getContent().get(0);
        assertEquals("Test Renovation", dto.getName());
        assertEquals("Test Desc", dto.getDescription());
        assertTrue(dto.isPublic());
        assertEquals(user.getId(), dto.getUserId());
    }

    @Test
    public void getUserRecords_withSearchTerm_returnsFilteredRecords() {
        User user = new User("test@example.com", "pass", "Test", "User");
        RenovationRecord record = new RenovationRecord(user, "Test Renovation", "Kitchen Table", List.of());
        record.setPublicity(true);
        record.setCreatedTimestamp(LocalDateTime.now());

        List<RenovationRecord> records = List.of(record);
        Page<RenovationRecord> mockPage = mock(Page.class);
        Pageable pageable = PageRequest.of(0, 10);

        when(mockPage.getContent()).thenReturn(records);
        when(renovationRecordRepository.findUserRecordsBySearch(eq(user), eq("Kitchen"), any(Pageable.class)))
                .thenReturn(mockPage);

        Page<RenovationRecordDTO> result = toTest.getPaginatedUserRecords(user, "Kitchen", null, pageable);

        verify(renovationRecordRepository).findUserRecordsBySearch(eq(user), eq("Kitchen"), any(Pageable.class));
        assertEquals(1, result.getContent().size());

        RenovationRecordDTO dto = result.getContent().get(0);
        assertEquals("Test Renovation", dto.getName());
        assertEquals("Kitchen Table", dto.getDescription());
        assertTrue(dto.isPublic());
        assertEquals(user.getId(), dto.getUserId());
    }

    @Test
    public void getPublicRecords_withNullTerm_returnsAllPublicRecords() {
        RenovationRecord record = new RenovationRecord();
        record.setName("Public Renovation");
        record.setDescription("Open to all");
        record.setPublicity(true);
        record.setCreatedTimestamp(LocalDateTime.now());
        record.setTags(List.of());

        List<RenovationRecord> records = List.of(record);
        Page<RenovationRecord> mockPage = mock(Page.class);
        Pageable pageable = PageRequest.of(0, 10);

        when(mockPage.getContent()).thenReturn(records);
        when(renovationRecordRepository.findPublicRecords(any(Pageable.class))).thenReturn(mockPage);

        Page<RenovationRecordDTO> result = toTest.getPaginatedPublicRecords("", null, pageable);

        verify(renovationRecordRepository).findPublicRecords(any(Pageable.class));
        assertEquals(1, result.getContent().size());
        assertEquals("Public Renovation", result.getContent().get(0).getName());
    }

    @Test
    public void getPublicRecords_withSearchTerm_returnsFilteredPublicRecords() {
        String term = "bathroom";
        RenovationRecord record = new RenovationRecord();
        record.setName("Bathroom Reno");
        record.setDescription("Full bathroom upgrade");
        record.setPublicity(true);
        record.setCreatedTimestamp(LocalDateTime.now());
        record.setTags(List.of());

        List<RenovationRecord> records = List.of(record);
        Page<RenovationRecord> mockPage = mock(Page.class);
        Pageable pageable = PageRequest.of(0, 10);

        when(mockPage.getContent()).thenReturn(records);
        when(renovationRecordRepository.findPublicRecordsBySearch(eq(term), any(Pageable.class))).thenReturn(mockPage);

        Page<RenovationRecordDTO> result = toTest.getPaginatedPublicRecords(term, null, pageable);

        verify(renovationRecordRepository).findPublicRecordsBySearch(eq(term), any(Pageable.class));
        assertEquals(1, result.getContent().size());
        assertEquals("Bathroom Reno", result.getContent().get(0).getName());
    }

    @Test
    public void getAllRecords_withNullTerm_returnsAllVisibleToUser() {
        User user = new User("test@example.com", "pass", "Test", "User");
        RenovationRecord record = new RenovationRecord(user, "Shared Reno", "Visible to user", List.of());
        record.setPublicity(false);
        record.setCreatedTimestamp(LocalDateTime.now());

        List<RenovationRecord> records = List.of(record);
        Page<RenovationRecord> mockPage = mock(Page.class);
        Pageable pageable = PageRequest.of(0, 10);

        when(mockPage.getContent()).thenReturn(records);
        when(renovationRecordRepository.findVisibleRecords(eq(user), any(Pageable.class))).thenReturn(mockPage);

        Page<RenovationRecordDTO> result = toTest.getPaginatedVisibleRecords(user, "", null, pageable);

        verify(renovationRecordRepository).findVisibleRecords(eq(user), any(Pageable.class));
        assertEquals(1, result.getContent().size());
        assertEquals("Shared Reno", result.getContent().get(0).getName());
    }

    @Test
    public void getAllRecords_withSearchTerm_returnsFilteredRecords() {
        User user = new User("test@example.com", "pass", "Test", "User");
        String term = "garage";
        RenovationRecord record = new RenovationRecord(user, "Garage Reno", "Converted to office", List.of());
        record.setPublicity(true);
        record.setCreatedTimestamp(LocalDateTime.now());

        List<RenovationRecord> records = List.of(record);
        Page<RenovationRecord> mockPage = mock(Page.class);
        Pageable pageable = PageRequest.of(0, 10);

        when(mockPage.getContent()).thenReturn(records);
        when(renovationRecordRepository.findVisibleRecordsBySearch(eq(user), eq(term), any(Pageable.class))).thenReturn(mockPage);

        Page<RenovationRecordDTO> result = toTest.getPaginatedVisibleRecords(user, term, null, pageable);

        verify(renovationRecordRepository).findVisibleRecordsBySearch(eq(user), eq(term), any(Pageable.class));
        assertEquals(1, result.getContent().size());
        assertEquals("Garage Reno", result.getContent().get(0).getName());
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

    private HashMap<LocalDate, List<RenovationTask>> getEmptyMapForRange(LocalDate startDate, LocalDate endDate) {
        LocalDate upperBoundary = endDate.plusDays(1);
        HashMap<LocalDate, List<RenovationTask>> dateMap = new HashMap<>();
        List<LocalDate> keys = startDate.datesUntil(upperBoundary).toList();
        keys.forEach(date -> dateMap.put(date, new ArrayList<>()));
        return dateMap;
    }

    @Test
    void generateCalendarCells_leapYearMonth_includesLeapDay() {
        LocalDate date = LocalDate.of(2024, 2, 1);
        LocalDate start = LocalDate.of(2024, 1, 28);
        LocalDate end = LocalDate.of(2024, 3, 2);
        when(renovationTaskService.getTasksWithinDates(any(RenovationRecord.class), any(LocalDate.class), any(LocalDate.class))).
                thenReturn(getEmptyMapForRange(start, end));
        List<RenovationTask> calendarTasks = List.of();

        CalendarCellDTO twentyNinthOfFebruary = new CalendarCellDTO(date.withDayOfMonth(29), calendarTasks);

        List<List<CalendarCellDTO>> calendarCells = toTest.generateCalendarCells(date, mockRenovationRecord);

        Assertions.assertTrue(calendarCells.get(4).contains(twentyNinthOfFebruary));
    }

    @Test
    void generateCalendarCells_monthStartingOnSunday_calendarHasSixRows() {
        LocalDate date = LocalDate.of(2023, 1, 1);

        LocalDate start = LocalDate.of(2022, 12, 26);
        LocalDate end = LocalDate.of(2023, 2, 5);
        when(renovationTaskService.getTasksWithinDates(any(RenovationRecord.class), any(LocalDate.class), any(LocalDate.class))).
                thenReturn(getEmptyMapForRange(start, end));

        List<RenovationTask> calendarTasks = List.of();

        CalendarCellDTO firstOfCurrentMonth = new CalendarCellDTO(date.withDayOfMonth(1), calendarTasks);
        CalendarCellDTO lastDayOfPreviousMonth = new CalendarCellDTO(date.withYear(2022).withMonth(12).withDayOfMonth(31), calendarTasks);
        CalendarCellDTO firstOfNextMonth = new CalendarCellDTO(date.withMonth(2).withDayOfMonth(1), calendarTasks);

        List<List<CalendarCellDTO>> calendarCells = toTest.generateCalendarCells(date, mockRenovationRecord);

        Assertions.assertTrue(calendarCells.get(0).contains(firstOfCurrentMonth));
        Assertions.assertTrue(calendarCells.get(0).contains(lastDayOfPreviousMonth));
        Assertions.assertTrue(calendarCells.get(5).contains(firstOfNextMonth));
        assertEquals(6, calendarCells.size());
    }

    @Test
    void generateCalendarCells_monthStartingOnMondayEndsOnSunday_calendarHasFourRows() {
        LocalDate date = LocalDate.of(2021, 2, 1);

        LocalDate start = LocalDate.of(2021, 2, 1);
        LocalDate end = LocalDate.of(2021, 2, 28);
        when(renovationTaskService.getTasksWithinDates(any(RenovationRecord.class), any(LocalDate.class), any(LocalDate.class))).
                thenReturn(getEmptyMapForRange(start, end));

        List<RenovationTask> calendarTasks = List.of();

        CalendarCellDTO firstOfCurrentMonth = new CalendarCellDTO(date.withMonth(2).withDayOfMonth(1), calendarTasks);
        CalendarCellDTO lastDayOfPreviousMonth = new CalendarCellDTO(date.withMonth(1).withDayOfMonth(31), calendarTasks);
        CalendarCellDTO firstOfNextMonth = new CalendarCellDTO(date.withMonth(3).withDayOfMonth(1), calendarTasks);

        List<List<CalendarCellDTO>> calendarCells = toTest.generateCalendarCells(date, mockRenovationRecord);

        Assertions.assertTrue(calendarCells.getFirst().contains(firstOfCurrentMonth));
        Assertions.assertFalse(calendarCells.getFirst().contains(lastDayOfPreviousMonth));
        Assertions.assertFalse(calendarCells.getLast().contains(firstOfNextMonth));
        assertEquals(4, calendarCells.size());
    }

    @Test
    void generateCalendarCells_monthStartingOnMondayDoesntEndOnSunday_calendarHasFiveRows() {
        LocalDate date = LocalDate.of(2021, 3, 1);

        LocalDate start = LocalDate.of(2021, 3, 1);
        LocalDate end = LocalDate.of(2021, 4, 4);
        when(renovationTaskService.getTasksWithinDates(any(RenovationRecord.class), any(LocalDate.class), any(LocalDate.class))).
                thenReturn(getEmptyMapForRange(start, end));

        List<RenovationTask> calendarTasks = List.of();

        CalendarCellDTO firstOfCurrentMonth = new CalendarCellDTO(date, calendarTasks);
        CalendarCellDTO lastDayOfPreviousMonth = new CalendarCellDTO(date.withMonth(2).withDayOfMonth(28), calendarTasks);
        CalendarCellDTO firstOfNextMonth = new CalendarCellDTO(date.withMonth(4).withDayOfMonth(1), calendarTasks);

        List<List<CalendarCellDTO>> calendarCells = toTest.generateCalendarCells(date, mockRenovationRecord);

        Assertions.assertTrue(calendarCells.get(0).contains(firstOfCurrentMonth));
        Assertions.assertFalse(calendarCells.get(0).contains(lastDayOfPreviousMonth));
        Assertions.assertTrue(calendarCells.get(4).contains(firstOfNextMonth));
        assertEquals(5, calendarCells.size());
    }
}

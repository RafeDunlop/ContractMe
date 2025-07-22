package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.TaskService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    private TaskService taskService;
    private RenovationRecord renovationRecord;
    private RenovationTaskRepository renovationTaskRepository;
    private RenovationTaskValidation renovationTaskValidation;

    @BeforeEach
    void setUp() {
        renovationTaskRepository = mock(RenovationTaskRepository.class);
        renovationTaskValidation = spy(RenovationTaskValidation.class);
        taskService = new TaskService(renovationTaskRepository, renovationTaskValidation);
        renovationRecord = new RenovationRecord();
    }

    @Test
    void returnTaskPages_whenNoTasks_returnsEmptyPage() {
        renovationRecord.setRenovationTasks(List.of());
        Pageable pageable = PageRequest.of(0, 5);

        Page<RenovationTask> result = taskService.returnTaskPages(renovationRecord, pageable);

        assertEquals(0L, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void returnTaskPages_whenSomeTasks_returnsCorrectPage() {
        renovationRecord.setRenovationTasks(createDummyTasks(10));
        Pageable pageable = PageRequest.of(1, 5);

        Page<RenovationTask> result = taskService.returnTaskPages(renovationRecord, pageable);

        assertEquals(5, result.getContent().size());
        assertEquals(10, result.getTotalElements());
    }

    @Test
    void returnTaskPages_whenPageSizeLargerThanTasks_returnsAllTasks() {
        renovationRecord.setRenovationTasks(createDummyTasks(3));

        Pageable pageable = PageRequest.of(0, 5);

        Page<RenovationTask> result = taskService.returnTaskPages(renovationRecord, pageable);

        assertEquals(3, result.getContent().size());
        assertEquals(3, result.getTotalElements());
    }

    private List<RenovationTask> createDummyTasks(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> new RenovationTask("Task " + i, "Description " + i, List.of("Room " + (i % 2 + 1)), LocalDate.now(), renovationRecord))
                .collect(Collectors.toList());
    }

    @Test
    public void addTask_allDetailsValid_callsSaveTask() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task 1", "New Task", LocalDate.now().plusDays(1).format(formatter), new ArrayList<>());
        RenovationRecord renovationRecord = mock(RenovationRecord.class);

        taskService.addRenovationTask(renovationTaskDTO, renovationRecord);

        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void validateTaskDetails_allDetailsAreValid_returnEmptyMap() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Tāsk Öné 2-3", "A".repeat(512), LocalDate.now().plusDays(1).format(formatter), new ArrayList<>());

        Map<String, List<String>> errors = taskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void validateTaskDetails_nameOnlyHasSpaces_returnNameFormatError() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("  ", "Some description", LocalDate.now().plusDays(1).format(formatter), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("nameError", List.of("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes."));

        Map<String, List<String>> errors = taskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_nameHasInvalidCharacters_returnNameFormatError() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One!", "Some description", LocalDate.now().plusDays(1).format(formatter), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("nameError", List.of("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes."));

        Map<String, List<String>> errors = taskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_descriptionOnlyHasSpaces_returnDescriptionEmptyError() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "  ", LocalDate.now().plusDays(1).format(formatter), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("descriptionError", List.of("Task description cannot be empty."));

        Map<String, List<String>> errors = taskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_descriptionIsTooLong_returnDescriptionLengthError() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "A".repeat(513), LocalDate.now().plusDays(1).format(formatter), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("descriptionError", List.of("Task description must be 512 characters or less."));

        Map<String, List<String>> errors = taskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_dueDateInPast_returnInvalidDueDateError() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "Some description", LocalDate.now().minusDays(1).format(formatter), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("dueDateError", List.of("Due date must be in the future."));

        Map<String, List<String>> errors = taskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_invalidFormatISO_returnInvalidDueDateError() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "Some description", LocalDate.now().plusDays(1).format(formatter), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("dueDateError", List.of("Date is not in valid format, DD/MM/YYYY."));

        Map<String, List<String>> errors = taskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }


    @Test
    public void validateTaskDetails_invalidFormatRandomChar_returnInvalidDueDateError() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "Some description", "NotADate", new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("dueDateError", List.of("Date is not in valid format, DD/MM/YYYY."));

        Map<String, List<String>> errors = taskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_roomsNotInRenovation_returnRoomError() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        renovationRecord.setRooms(List.of("room1", "room2", "room3"));
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO(
                "Task One",
                "Some description",
                LocalDate.now().plusDays(1).format(formatter),
                List.of("room1", "room2", "notRoom3")
        );
        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("roomError", List.of("Whoops, it looks like \"notRoom3\" is not a valid room anymore"));
        assertEquals(expectedErrors, taskService.validateTaskDetails(renovationTaskDTO, renovationRecord));
    }

    @Test
    public void getTasksWithinDates_noTasksBetweenDates_mapContainsEmptyLists() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        when(renovationTaskRepository.getByDueDateBetween(startDate, endDate, renovationRecord)).thenReturn(List.of());
        Map<LocalDate,  List<RenovationTask>> toTest = taskService.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertEquals(3, toTest.size());

        for (int i = 0; i < toTest.size(); i++) {
            assertTrue(toTest.get(startDate.plusDays(i)).isEmpty());
        }

    }

    @Test
    public void getTasksWithinDates_middleDayHasTask_taskInMap() {
        RenovationTask dummyTask = mock(RenovationTask.class);
        LocalDate startDate = LocalDate.now();
        LocalDate middleDate = startDate.plusDays(1);
        LocalDate endDate = startDate.plusDays(2);
        when(dummyTask.getDueDate()).thenReturn(startDate.plusDays(1));
        when(dummyTask.getRenovationRecord()).thenReturn(renovationRecord);
        when(renovationTaskRepository.getByDueDateBetween(any(LocalDate.class), any(LocalDate.class), any(RenovationRecord.class))).thenReturn(List.of(dummyTask));
        Map<LocalDate,  List<RenovationTask>> toTest = taskService.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertEquals(dummyTask, toTest.get(middleDate).getFirst());
    }

    @Test
    public void getTasksWithinDates_oneDay_taskInMap() {
        RenovationTask dummyTask = mock(RenovationTask.class);
        LocalDate startDate = LocalDate.now();
        when(dummyTask.getDueDate()).thenReturn(startDate);
        when(dummyTask.getRenovationRecord()).thenReturn(renovationRecord);
        when(renovationTaskRepository.getByDueDateBetween(any(LocalDate.class), any(LocalDate.class), any(RenovationRecord.class))).thenReturn(List.of(dummyTask));
        Map<LocalDate,  List<RenovationTask>> toTest = taskService.getTasksWithinDates(renovationRecord, startDate, startDate);
        assertEquals(dummyTask, toTest.get(startDate).getFirst());
    }

    @Test
    public void getTasksWithinDates_endBeforeStart_emptyMap() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusDays(1);
        when(renovationTaskRepository.getByDueDateBetween(any(LocalDate.class), any(LocalDate.class), any(RenovationRecord.class))).thenReturn(List.of());
        Map<LocalDate,  List<RenovationTask>> toTest = taskService.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertEquals(0, toTest.size());
    }

    @Test
    public void getTasksWithinDates_firstDateMultipleTasks_tasksInMap() {
        RenovationTask dummyTask1 = mock(RenovationTask.class);
        RenovationTask dummyTask2 = mock(RenovationTask.class);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(1);
        when(dummyTask1.getRenovationRecord()).thenReturn(renovationRecord);
        when(dummyTask2.getRenovationRecord()).thenReturn(renovationRecord);
        when(dummyTask1.getDueDate()).thenReturn(startDate);
        when(dummyTask2.getDueDate()).thenReturn(startDate);
        when(renovationTaskRepository.getByDueDateBetween(any(LocalDate.class), any(LocalDate.class), any(RenovationRecord.class))).thenReturn(List.of(dummyTask1, dummyTask2));
        Map<LocalDate,  List<RenovationTask>> toTest = taskService.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertEquals(2, toTest.get(startDate).size());
        assertEquals(dummyTask1, toTest.get(startDate).get(0));
        assertEquals(dummyTask2, toTest.get(startDate.plusDays(0)).get(1));

    }

    @Test
    public void getTasksWithinDates_edgeDatesHaveTasks_allTasksInMap() {
        RenovationTask dummyTask1 = mock(RenovationTask.class);
        RenovationTask dummyTask2 = mock(RenovationTask.class);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        when(dummyTask1.getRenovationRecord()).thenReturn(renovationRecord);
        when(dummyTask2.getRenovationRecord()).thenReturn(renovationRecord);
        when(dummyTask1.getDueDate()).thenReturn(startDate);
        when(dummyTask2.getDueDate()).thenReturn(endDate);
        when(renovationTaskRepository.getByDueDateBetween(any(LocalDate.class), any(LocalDate.class), any(RenovationRecord.class))).thenReturn(List.of(dummyTask1, dummyTask2));
        Map<LocalDate,  List<RenovationTask>> toTest = taskService.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertEquals(dummyTask1, toTest.get(startDate).getFirst());
        assertEquals(dummyTask2, toTest.get(endDate).getFirst());
    }

    @Test
    public void updateTask_validDTO_savesTask() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        RenovationTaskDTO dto = new RenovationTaskDTO("Task 1", "Desc", LocalDate.now().format(formatter), new ArrayList<>());
        RenovationTask task = mock(RenovationTask.class);

        taskService.updateTask(dto, task);

        verify(task).setName("Task 1");
        verify(task).setDescription("Desc");
        verify(task).setDueDate(LocalDate.parse(dto.getDueDate(),formatter));
        verify(task).setRoomList(dto.getRooms());
        verify(renovationTaskRepository, times(1)).save(task);
    }

    @Test
    public void updateTask_nullDTO_throwsException() {
        RenovationTask task = mock(RenovationTask.class);
        assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(null, task);
        });
    }

    @Test
    public void updateIcon_fileDoesNotExist_throwsException() {
            assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTaskIcon(mock(RenovationTask.class), "badfile.png");
        });
    }

    @Test
    public void updateIcon_fileExists_savesTask() {
        when(renovationTaskValidation.validateTaskIconFileName("goodfile.png")).thenReturn(true);
        RenovationTask task = mock(RenovationTask.class);
        taskService.updateTaskIcon(task, "goodfile.png");
        verify(renovationTaskRepository, times(1)).save(task);
    }
}


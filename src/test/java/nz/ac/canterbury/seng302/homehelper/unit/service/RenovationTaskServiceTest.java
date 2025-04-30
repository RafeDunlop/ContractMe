package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class RenovationTaskServiceTest {

    private RenovationTaskService renovationTaskService;
    private RenovationRecord renovationRecord;
    private RenovationTaskRepository renovationTaskRepository;
    private RenovationTaskValidation renovationTaskValidation;

    @BeforeEach
    void setUp() {
        renovationTaskRepository = Mockito.mock(RenovationTaskRepository.class);
        renovationTaskValidation = new RenovationTaskValidation();
        renovationTaskService = new RenovationTaskService(renovationTaskRepository, renovationTaskValidation);
        renovationRecord = new RenovationRecord();
    }

    @Test
    void returnTaskPages_whenNoTasks_returnsEmptyPage() {
        renovationRecord.setRenovationTasks(List.of());
        Pageable pageable = PageRequest.of(0, 5);

        Page<RenovationTask> result = renovationTaskService.returnTaskPages(renovationRecord, pageable);

        assertEquals(0L, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void returnTaskPages_whenSomeTasks_returnsCorrectPage() {
        renovationRecord.setRenovationTasks(createDummyTasks(10));
        Pageable pageable = PageRequest.of(1, 5);

        Page<RenovationTask> result = renovationTaskService.returnTaskPages(renovationRecord, pageable);

        assertEquals(5, result.getContent().size());
        assertEquals(10, result.getTotalElements());
    }

    @Test
    void returnTaskPages_whenPageSizeLargerThanTasks_returnsAllTasks() {
        renovationRecord.setRenovationTasks(createDummyTasks(3));

        Pageable pageable = PageRequest.of(0, 5);

        Page<RenovationTask> result = renovationTaskService.returnTaskPages(renovationRecord, pageable);

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
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task 1", "New Task", LocalDate.now().plusDays(1), new ArrayList<>());
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);

        renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord);

        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void validateTaskDetails_allDetailsAreValid_returnEmptyMap() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Tāsk Öné 2-3", "A".repeat(512), LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> errors = renovationTaskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void validateTaskDetails_nameOnlyHasSpaces_returnNameFormatError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("  ", "Some description", LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("nameError", List.of("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes."));

        Map<String, List<String>> errors = renovationTaskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_nameHasInvalidCharacters_returnNameFormatError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One!", "Some description", LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("nameError", List.of("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes."));

        Map<String, List<String>> errors = renovationTaskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_descriptionOnlyHasSpaces_returnDescriptionEmptyError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "  ", LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("descriptionError", List.of("Task description cannot be empty."));

        Map<String, List<String>> errors = renovationTaskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_descriptionIsTooLong_returnDescriptionLengthError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "A".repeat(513), LocalDate.now().plusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("descriptionError", List.of("Task description must be 512 characters or less."));

        Map<String, List<String>> errors = renovationTaskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }

    @Test
    public void validateTaskDetails_dueDateInPast_returnInvalidDueDateError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task One", "Some description", LocalDate.now().minusDays(1), new ArrayList<>());

        Map<String, List<String>> expectedErrors = new HashMap<>();
        expectedErrors.put("dueDateError", List.of("Due date must be in the future."));

        Map<String, List<String>> errors = renovationTaskService.validateTaskDetails(renovationTaskDTO, renovationRecord);
        assertEquals(expectedErrors, errors);
    }
}


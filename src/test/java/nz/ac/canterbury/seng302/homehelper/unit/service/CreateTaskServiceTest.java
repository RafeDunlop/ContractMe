package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationValidation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateTaskServiceTest {

    @Mock
    private RenovationValidation renovationValidation;
    @Mock
    private RenovationTaskRepository renovationTaskRepository;

    @Test
    public void addTask_allDetailsValid_callsSaveTask() {
        RenovationTaskService renovationTaskService = new RenovationTaskService(renovationTaskRepository);
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task 1", "New Task", LocalDate.now().plusDays(1), new ArrayList<>());
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);

        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(Map.of());

        renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord);

        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void addTask_taskNameInvalid_returnsError() {
        RenovationTaskService renovationTaskService = new RenovationTaskService(renovationTaskRepository);
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("@#$%", "Valid description", LocalDate.now().plusDays(1), new ArrayList<>());
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);

        Map<String, List<String>> errors = Map.of(
                "nameError", List.of("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.")
        );

        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(errors);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord)
        );

        assertEquals("Task name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.", exception.getMessage());
    }

    @Test
    public void addTask_taskDescriptionEmpty_returnsError() {
        RenovationTaskService renovationTaskService = new RenovationTaskService(renovationTaskRepository);
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Valid Name", " ", LocalDate.now().plusDays(1), new ArrayList<>());
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);

        Map<String, List<String>> errors = Map.of(
                "descriptionError", List.of("Task description cannot be empty.")
        );

        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(errors);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord)
        );

        assertEquals("Task description cannot be empty.", exception.getMessage());
    }

    @Test
    public void addTask_taskDescriptionOver512Characters_returnsError() {
        RenovationTaskService renovationTaskService = new RenovationTaskService(renovationTaskRepository);
        StringBuilder longDescription = new StringBuilder();
        for (int i = 0; i < 513; i++) {
            longDescription.append("a");
        }
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task Name", longDescription.toString(), LocalDate.now().plusDays(1), new ArrayList<>());
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);

        Map<String, List<String>> errors = Map.of(
                "descriptionError", List.of("Task description must be 512 characters or less.")
        );

        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(errors);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord)
        );

        assertEquals("Task description must be 512 characters or less.", exception.getMessage());
    }


    @Test
    public void addTask_dueDateInPast_returnsError() {
        RenovationTaskService renovationTaskService = new RenovationTaskService(renovationTaskRepository);
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task Name", "New Task", LocalDate.now().minusDays(1), new ArrayList<>());
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);

        Map<String, List<String>> errors = new HashMap<>();
        errors.put("dueDateError", List.of("Due date must be in the future."));
        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(errors);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord);
        });

        assertEquals("Due date must be in the future.", exception.getMessage());
    }
}

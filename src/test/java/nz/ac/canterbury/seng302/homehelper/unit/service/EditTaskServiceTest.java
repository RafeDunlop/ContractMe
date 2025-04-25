package nz.ac.canterbury.seng302.homehelper.unit.service;

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.EditTaskService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationValidation;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EditTaskServiceTest {

    private RenovationTaskRepository renovationTaskRepository;
    private RenovationValidation renovationValidation;
    private EditTaskService editTaskService;

    @BeforeEach
    void setUp() {
        renovationTaskRepository = Mockito.mock(RenovationTaskRepository.class);
        renovationValidation = Mockito.mock(RenovationValidation.class);
        editTaskService = new EditTaskService(renovationTaskRepository, renovationValidation);
    }

    @Test
    public void EditTask_allDetailsValid_callsSaveTask() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task 1", "New Task", null,new ArrayList<>());
        RenovationTask renovationTask = Mockito.mock(RenovationTask.class);
        editTaskService.updateTask(renovationTaskDTO, renovationTask);
        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void addTask_taskNameInvalid_returnsError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("@#$%", "New Task", null,new ArrayList<>());
        RenovationTask renovationTask = Mockito.mock(RenovationTask.class);
        List<String> errors = new ArrayList<>();
        errors.add("name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.");
        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(errors);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {editTaskService.updateTask(renovationTaskDTO, renovationTask);});
        assertEquals("name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.", exception.getMessage());
    }

    @Test
    public void addTask_taskDescriptionEmpty_returnsError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("@#$%", "", null,new ArrayList<>());
        RenovationTask renovationTask = Mockito.mock(RenovationTask.class);
        List<String> errors = new ArrayList<>();
        errors.add("description cannot be empty.");
        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(errors);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {editTaskService.updateTask(renovationTaskDTO, renovationTask);});
        assertEquals("description cannot be empty.", exception.getMessage());
    }

    @Test
    public void addTask_taskDescriptionOver512Characters_returnsError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task Name", """
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
                aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa""", null,new ArrayList<>());
        RenovationTask renovationTask = Mockito.mock(RenovationTask.class);
        List<String> errors = new ArrayList<>();
        errors.add("description must be 512 characters or less.");
        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(errors);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {editTaskService.updateTask(renovationTaskDTO, renovationTask);});
        assertEquals("description must be 512 characters or less.", exception.getMessage());
    }

    @Test
    public void addTask_dueDateInPast_returnsError() {
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task Name", "New Task", LocalDate.now().minusDays(1),new ArrayList<>());
        RenovationTask renovationTask = Mockito.mock(RenovationTask.class);
        List<String> errors = new ArrayList<>();
        errors.add("Due date must be in the future.");
        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(errors);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {editTaskService.updateTask(renovationTaskDTO, renovationTask);});
        assertEquals("Due date must be in the future.", exception.getMessage());
    }

    @Test
    public void updateIcon_fileDoesNotExists_throwsException() {
        when(renovationValidation.validateTaskIconFileName(Mockito.anyString())).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> {
            editTaskService.updateTaskIcon(Mockito.mock(RenovationTask.class), "nonexistentfile.png");
        });
    }

    @Test
    public void updateIcon_fileExists_savesTask() {
        when(renovationValidation.validateTaskIconFileName(Mockito.anyString())).thenReturn(true);
        RenovationTask renovationTask = Mockito.mock(RenovationTask.class);
        editTaskService.updateTaskIcon(renovationTask, "existingfile.png");
        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(renovationTask);
    }
}

package nz.ac.canterbury.seng302.homehelper.integration.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.EditTaskService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationValidation;

public class EditTaskServiceIntegrationTest {
    private EditTaskService editTaskService;
    private RenovationTaskRepository renovationTaskRepository;

    @BeforeEach
    public void setUp() {
        renovationTaskRepository = mock(RenovationTaskRepository.class);
        editTaskService = new EditTaskService(renovationTaskRepository, new RenovationValidation());
    }

    @Test
    public void updateIcon_fileExists_savesTask() {
        RenovationTask renovationTask = new RenovationTask("Task 1", "New Task", null, null, null);
        // Default default-icon.png should exist in test resources
        assertDoesNotThrow(() -> editTaskService.updateTaskIcon(renovationTask, "default-icon.png"));
        verify(renovationTaskRepository, times(1)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    public void updateIcon_fileDoesNotExist_throwsException() {
        RenovationTask renovationTask = new RenovationTask("Task 1", "New Task", null, null, null);
        assertThrows(IllegalArgumentException.class, () -> editTaskService.updateTaskIcon(renovationTask, "non-existent-file.png"));
        verify(renovationTaskRepository, never()).save(Mockito.any(RenovationTask.class));
    }
}

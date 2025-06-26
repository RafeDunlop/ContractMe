package nz.ac.canterbury.seng302.homehelper.unit.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.EditTaskService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class EditTaskServiceTest {

    private RenovationTaskRepository renovationTaskRepository;
    private RenovationTaskValidation renovationTaskValidation;
    private EditTaskService editTaskService;

    @BeforeEach
    void setUp() {
        renovationTaskRepository = mock(RenovationTaskRepository.class);
        renovationTaskValidation = mock(RenovationTaskValidation.class);
        editTaskService = new EditTaskService(renovationTaskRepository, renovationTaskValidation);
    }

    @Test
    public void updateTask_validDTO_savesTask() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        RenovationTaskDTO dto = new RenovationTaskDTO("Task 1", "Desc", LocalDate.now().format(formatter), new ArrayList<>());
        RenovationTask task = mock(RenovationTask.class);

        editTaskService.updateTask(dto, task);

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
            editTaskService.updateTask(null, task);
        });
    }

    @Test
    public void updateIcon_fileDoesNotExist_throwsException() {
        when(renovationTaskValidation.validateTaskIconFileName("badfile.png")).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> {
            editTaskService.updateTaskIcon(mock(RenovationTask.class), "badfile.png");
        });
    }

    @Test
    public void updateIcon_fileExists_savesTask() {
        when(renovationTaskValidation.validateTaskIconFileName("goodfile.png")).thenReturn(true);
        RenovationTask task = mock(RenovationTask.class);
        editTaskService.updateTaskIcon(task, "goodfile.png");
        verify(renovationTaskRepository, times(1)).save(task);
    }
}
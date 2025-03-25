package nz.ac.canterbury.seng302.homehelper.unit.service;

import jakarta.xml.bind.ValidationException;
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

import java.util.ArrayList;
import java.util.List;

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
        RenovationTaskService renovationTaskService = new RenovationTaskService(renovationTaskRepository, renovationValidation);
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task 1", "New Task", null);
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);
        List<String> roomList = new ArrayList<>();
        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(new ArrayList<>());
        renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord, roomList);
        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(Mockito.any());

    }


    @Test
    public void addTask_detailsInvalid_returnsError() {
        RenovationTaskService renovationTaskService = new RenovationTaskService(renovationTaskRepository, renovationValidation);
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("@#$%", "New Task", null);
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);
        List<String> roomList = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        errors.add("name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.");
        when(renovationValidation.validateTaskDetails(Mockito.any())).thenReturn(errors);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord, roomList);});
        assertEquals("name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes.", exception.getMessage());

    }



}

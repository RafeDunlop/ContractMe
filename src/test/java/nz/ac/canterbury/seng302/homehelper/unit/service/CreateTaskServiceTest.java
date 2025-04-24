package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class CreateTaskServiceTest {

    @Mock
    private RenovationTaskRepository renovationTaskRepository;

    @Test
    public void addTask_allDetailsValid_callsSaveTask() {
        RenovationTaskService renovationTaskService = new RenovationTaskService(renovationTaskRepository);
        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("Task 1", "New Task", LocalDate.now().plusDays(1), new ArrayList<>());
        RenovationRecord renovationRecord = Mockito.mock(RenovationRecord.class);

        renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord);

        Mockito.verify(renovationTaskRepository, Mockito.times(1)).save(Mockito.any());
    }
}

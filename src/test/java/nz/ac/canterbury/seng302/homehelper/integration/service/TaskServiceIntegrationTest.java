package nz.ac.canterbury.seng302.homehelper.integration.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.TaskService;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceIntegrationTest {

    private TaskService toTest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationTaskRepository renovationTaskRepository;

    @Autowired
    private RenovationTaskValidation renovationTaskValidation;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    private RenovationRecord renovationRecord;

    @BeforeEach
    void setUp() {
        toTest = new TaskService(renovationTaskRepository, renovationTaskValidation);
        User testUser = new User(
                "firstName",
                "lastName",
                "email@test.com",
                "P4$$word"
        );
        testUser = userRepository.save(testUser);
        renovationRecord = new RenovationRecord(
                testUser,
                "name",
                "description",
                List.of()
        );
        renovationRecord = renovationRecordRepository.save(renovationRecord);
    }

    @AfterEach
    void breakDown() {
        renovationTaskRepository.deleteAll();
        renovationTaskRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    public void getTasksWithinDates_noTasksBetweenDates_mapContainsEmptyLists() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        Map<LocalDate,  List<RenovationTask>> map = this.toTest.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertEquals(3, map.size());
    }

    @Test
    @Transactional
    public void getTasksWithinDates_taskDueBeforeStart_notRetrieved() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        RenovationTask testTask = new RenovationTask(
                "name",
                "description",
                List.of(),
                startDate.minusDays(1),
                renovationRecord
        );
        renovationTaskRepository.save(testTask);
        Map<LocalDate,  List<RenovationTask>> map = this.toTest.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertFalse(map.entrySet().stream().anyMatch(entry -> entry.getValue().contains(testTask)));
    }

    @Test
    @Transactional
    public void getTasksWithinDates_taskDueAfterEnd_notRetrieved() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        RenovationTask testTask = new RenovationTask(
                "name",
                "description",
                List.of(),
                endDate.plusDays(1),
                renovationRecord
        );
        renovationTaskRepository.save(testTask);
        Map<LocalDate,  List<RenovationTask>> map = this.toTest.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertFalse(map.entrySet().stream().anyMatch(entry -> entry.getValue().contains(testTask)));
    }

    @Test
    @Transactional
    public void getTasksWithinDates_taskDueOnStartDate_retrieved() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        RenovationTask testTask = new RenovationTask(
                "name",
                "description",
                List.of(),
                startDate,
                renovationRecord
        );
        renovationTaskRepository.save(testTask);
        Map<LocalDate,  List<RenovationTask>> map = this.toTest.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertEquals(testTask, map.get(startDate).getFirst());
    }

    @Test
    @Transactional
    public void getTasksWithinDates_taskDueOnEndDate_retrieved() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(2);
        RenovationTask testTask = new RenovationTask(
                "name",
                "description",
                List.of(),
                endDate,
                renovationRecord
        );
        renovationTaskRepository.save(testTask);
        Map<LocalDate,  List<RenovationTask>> map = this.toTest.getTasksWithinDates(renovationRecord, startDate, endDate);
        assertEquals(testTask, map.get(endDate).getFirst());
    }

    @Test
    @Transactional
    public void updateIcon_fileExists_savesTask() {
        RenovationTask renovationTask = new RenovationTask("Task 1", "New Task", List.of(), LocalDate.now(), renovationRecord);
        renovationTaskRepository.save(renovationTask);
        // Default default-icon.png should exist in test resources
        assertDoesNotThrow(() -> toTest.updateTaskIcon(renovationTask, "default-icon.png"));
        verify(renovationTaskRepository, times(1)).save(Mockito.any(RenovationTask.class));
    }

    @Test
    @Transactional
    public void updateIcon_fileDoesNotExist_throwsException() {
        RenovationTask renovationTask = new RenovationTask("Task 1", "New Task", List.of(), LocalDate.now(), renovationRecord);
        renovationTaskRepository.save(renovationTask);
        assertThrows(IllegalArgumentException.class, () -> toTest.updateTaskIcon(renovationTask, "non-existent-file.png"));
        verify(renovationTaskRepository, never()).save(Mockito.any(RenovationTask.class));
    }
}

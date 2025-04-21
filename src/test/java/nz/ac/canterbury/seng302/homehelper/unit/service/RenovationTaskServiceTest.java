package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class RenovationTaskServiceTest {

    private RenovationTaskService renovationTaskService;
    private RenovationRecord renovationRecord;
    private RenovationTaskRepository renovationTaskRepository;

    @BeforeEach
    void setUp() {
        renovationTaskService = new RenovationTaskService(renovationTaskRepository);
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

}

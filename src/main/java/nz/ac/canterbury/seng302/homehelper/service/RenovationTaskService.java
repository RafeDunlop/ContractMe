package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RenovationTaskService {

    Logger logger = LoggerFactory.getLogger(RenovationTaskService.class);

    private final RenovationValidation renovationValidation;
    private final RenovationTaskRepository renovationTaskRepository;

    /**
     * Constructor for RenovationTaskService class
     * @param renovationTaskRepository initialises the repo for storing tasks
     */
    @Autowired
    public RenovationTaskService(RenovationTaskRepository renovationTaskRepository, RenovationValidation renovationValidation) {
        this.renovationTaskRepository = renovationTaskRepository;
        this.renovationValidation = renovationValidation;
    }

    /**
     * Adds a new renovation task to the repository
     */
    public void addRenovationTask(RenovationTaskDTO renovationTaskDTO, RenovationRecord renovationRecord, List<String> roomList) {
        List<String> errors = new ArrayList<>();
        errors = renovationValidation.validateTaskDetails(renovationTaskDTO);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        String name = renovationTaskDTO.getName();
        String description = renovationTaskDTO.getDescription();
        LocalDate dueDate = renovationTaskDTO.getDueDate();
        RenovationTask renovationTask = new RenovationTask(name, description, roomList, dueDate, renovationRecord);

        renovationTaskRepository.save(renovationTask);
    }

    public Page<RenovationTask> returnTaskPages(RenovationRecord renovationRecord, Pageable pageable ) {
        List<RenovationTask> taskSubList = new ArrayList<>();
        List<RenovationTask> tasks = renovationRecord.getRenovationTasks();

        if (tasks == null || tasks.isEmpty()) {
            return new PageImpl<>(taskSubList, pageable, 0); // Return an empty page
        }

        int startIndex =(int) pageable.getOffset();
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (startIndex >= tasks.size()) {
            startIndex = tasks.size() - pageable.getPageSize();
        }
        int endIndex = Math.min(startIndex + pageable.getPageSize(), tasks.size());

        taskSubList = tasks.subList(startIndex, endIndex);
        return new PageImpl<>(taskSubList, pageable, tasks.size());
    }
}

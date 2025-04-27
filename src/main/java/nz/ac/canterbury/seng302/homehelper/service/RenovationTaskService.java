package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class RenovationTaskService {

    Logger logger = LoggerFactory.getLogger(RenovationTaskService.class);

    private final RenovationTaskRepository renovationTaskRepository;
    private final RenovationTaskValidation renovationTaskValidation;

    /**
     * Constructor for RenovationTaskService class
     * @param renovationTaskRepository initialises the repo for storing tasks
     */
    @Autowired
    public RenovationTaskService(RenovationTaskRepository renovationTaskRepository, RenovationTaskValidation renovationTaskValidation) {
        this.renovationTaskRepository = renovationTaskRepository;
        this.renovationTaskValidation = renovationTaskValidation;
    }

    public RenovationTask getTaskById(Long id) {
        return renovationTaskRepository.findById(id).orElse(null);
    }

    /**
     * Adds a new renovation task to the repository
     */
    public void addRenovationTask(RenovationTaskDTO renovationTaskDTO, RenovationRecord renovationRecord) {

        String name = renovationTaskDTO.getName();
        String description = renovationTaskDTO.getDescription();
        LocalDate dueDate = renovationTaskDTO.getDueDate();
        List<String> roomList = renovationTaskDTO.getRooms();
        RenovationTask renovationTask = new RenovationTask(name, description, roomList, dueDate, renovationRecord);

        renovationTaskRepository.save(renovationTask);
    }

    /**
     * Returns a paginated list of tasks for the given record.
     * @param renovationRecord The renovation record containing the list of tasks to be paginated.
     * @param pageable spring pagination information, including the offset and page size.
     * @return A page of tasks for the renovation record. If there are no tasks an empty page is returned.
     */
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

    public List<String> getTaskIconFilenames() {
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            List<String> taskIconNames = new ArrayList<>(Arrays.stream(pathMatchingResourcePatternResolver.getResources("/static/images/*")).map(Resource::getFilename).toList());
            taskIconNames.remove("default-icon.png");
            return taskIconNames;
        } catch (IOException e) {
            logger.error("Error while trying to get icon filenames", e);
            return Collections.emptyList();
        }
    }

    /**
     * Validates the details of the task inputted by the user. Checks to see if all the details are valid and returns a map
     * of error messages for each invalid detail.
     * @return A map of errors generated from validating the task details
     */
    public Map<String, List<String>> validateTaskDetails(RenovationTaskDTO renovationTaskDTO) {
        Map<String, List<String>> errors = new HashMap<>();
        String errorMessageType = "Task";

        String nameError = renovationTaskValidation.validateName(renovationTaskDTO.getName(), errorMessageType);
        putIfNotEmpty(errors, "nameError", nameError == null ? null : List.of(nameError));

        String descriptionError = renovationTaskValidation.validateDescription(renovationTaskDTO.getDescription(), errorMessageType);
        putIfNotEmpty(errors, "descriptionError", descriptionError == null ? null : List.of(descriptionError));

        String dueDateError = renovationTaskValidation.validateDueDate(renovationTaskDTO.getDueDate());
        putIfNotEmpty(errors, "dueDateError", dueDateError == null ? null : List.of(dueDateError));

        return errors;
    }

    /**
     * Inserts a key-value pair into the provided map if the list of messages is not null or empty.
     * @param map       the map to insert the key-value pair into
     * @param key       the key to associate with the messages
     * @param messages  the list of error messages to insert if not empty
     */
    private void putIfNotEmpty(Map<String, List<String>> map, String key, List<String> messages) {
        if (messages != null && !messages.isEmpty()) {
            map.put(key, messages);
        }
    }
}

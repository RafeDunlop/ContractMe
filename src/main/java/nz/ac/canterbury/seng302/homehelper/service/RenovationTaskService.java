package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.TaskState;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.util.MapUtil;
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
import java.time.format.DateTimeFormatter;
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

    /**
     * Gets the task with the corresponding id
     * @param id The id corresponding to the {@code RenovationTask} to be retrieved
     * @return The {RenovationTask} corresponding to the specified id
     */
    public RenovationTask getTaskById(Long id) {
        return renovationTaskRepository.findById(id).orElse(null);
    }


    /**
     * Gets a mapping of dates within the specified range to tasks whose due dates fall on those dates
     * @param renovationRecord The {@code RenovationRecord} whose tasks are being queried
     * @param startDate The first date for which to retrieve {@code RenovationTask} objects
     * @param endDate The last date for which to retrieve {@code RenovationTask} objects
     * @return A map which contains the {@code RenovationTask} objects associated with each date within the specified range
     */
    public Map<LocalDate, List<RenovationTask>> getTasksWithinDates(RenovationRecord renovationRecord, LocalDate startDate, LocalDate endDate) {
        LocalDate upperBoundary = endDate.plusDays(1);
        HashMap<LocalDate, List<RenovationTask>> dateMap = new HashMap<>();
        List<LocalDate> keys = startDate.datesUntil(upperBoundary).toList();
        keys.forEach(date -> dateMap.put(date, new ArrayList<>()));
        Iterator<LocalDate> keyItr = keys.iterator();
        Iterator<RenovationTask> valItr = renovationTaskRepository.getByDueDateBetween(startDate, endDate, renovationRecord).iterator();
        if (keyItr.hasNext() && valItr.hasNext()) {
            LocalDate currentKey = keyItr.next();
            do {
                RenovationTask renovationTask = valItr.next();
                while (!currentKey.equals(renovationTask.getDueDate())) {
                    currentKey = keyItr.next();
                }
                dateMap.get(currentKey).add(renovationTask);
            } while(valItr.hasNext());
        }
        return dateMap;
    }

    /**
     * Adds a new renovation task to the repository
     */
    public void addRenovationTask(RenovationTaskDTO renovationTaskDTO, RenovationRecord renovationRecord) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TaskState state = TaskState.NOT_STARTED;
        String name = renovationTaskDTO.getName();
        String description = renovationTaskDTO.getDescription();
        LocalDate dueDate = null;
        if (renovationTaskDTO.getDueDate() != null && !renovationTaskDTO.getDueDate().isBlank()) {
            dueDate = LocalDate.parse(renovationTaskDTO.getDueDate(),formatter);
        }
        List<String> roomList = renovationTaskDTO.getRooms();
        RenovationTask renovationTask = new RenovationTask(name, description, roomList, dueDate, renovationRecord);
        renovationTask.setState(state);
        renovationTaskRepository.save(renovationTask);
    }

    /**
     * Returns a paginated list of tasks for the given record.
     * @param renovationRecord The renovation record containing the list of tasks to be paginated.
     * @param pageable spring pagination information, including the offset and page size.
     * @return A page of tasks for the renovation record. If there are no tasks an empty page is returned.
     */
    public Page<RenovationTask> returnTaskPages(RenovationRecord renovationRecord, Pageable pageable, String status) {
        TaskState state;
        List<RenovationTask> tasks;
        try {
            state = TaskState.fromCamelCaseName(status);
            tasks = renovationTaskRepository.findByRenovationRecordAndState(renovationRecord, state);
        } catch (IllegalArgumentException e) {
            tasks = renovationRecord.getRenovationTasks();
        }

        if (tasks == null || tasks.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        int totalTasks = tasks.size();
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), totalTasks);

        // Guard against out-of-bounds start index
        if (startIndex >= totalTasks) {
            return new PageImpl<>(Collections.emptyList(), pageable, totalTasks);
        }

        List<RenovationTask> taskSubList = tasks.subList(startIndex, endIndex);
        return new PageImpl<>(taskSubList, pageable, totalTasks);
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
    public Map<String, List<String>> validateTaskDetails(RenovationTaskDTO renovationTaskDTO, RenovationRecord renovationRecord) {
        Map<String, List<String>> errors = new HashMap<>();
        String errorMessageType = "Task";

        List<String> nameError = renovationTaskValidation.validateName(renovationTaskDTO.getName(), errorMessageType);
        MapUtil.putIfNotEmpty(errors, "nameError", nameError);

        String roomError = renovationTaskValidation.validateRooms(renovationRecord, renovationTaskDTO.getRooms());
        MapUtil.putIfNotEmpty(errors, "roomError", (roomError == null) ? null : List.of(roomError));

        String descriptionError = renovationTaskValidation.validateDescription(renovationTaskDTO.getDescription(), errorMessageType);
        MapUtil.putIfNotEmpty(errors, "descriptionError", (descriptionError == null) ? null : List.of(descriptionError));

        String dueDateError = renovationTaskValidation.validateDueDate(renovationTaskDTO.getDueDate());
        MapUtil.putIfNotEmpty(errors, "dueDateError", (dueDateError == null) ? null : List.of(dueDateError));

        return errors;
    }
}

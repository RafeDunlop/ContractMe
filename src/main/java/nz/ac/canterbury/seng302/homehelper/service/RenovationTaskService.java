package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationValidation;
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

    /**
     * Constructor for RenovationTaskService class
     * @param renovationTaskRepository initialises the repo for storing tasks
     */
    @Autowired
    public RenovationTaskService(RenovationTaskRepository renovationTaskRepository) {
        this.renovationTaskRepository = renovationTaskRepository;
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
}

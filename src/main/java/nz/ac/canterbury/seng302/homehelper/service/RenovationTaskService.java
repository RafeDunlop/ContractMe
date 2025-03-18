package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RenovationTaskService {

    Logger logger = LoggerFactory.getLogger(RenovationTaskService.class);

    private static final int maximumDescriptionLength = 512;

    private final RenovationTaskRepository renovationTaskRepository;

    /**
     * Constructor for RenovationTaskService class
     * @param renovationTaskRepository initialises the repo for storing tasks
     */
    @Autowired
    public RenovationTaskService(RenovationTaskRepository renovationTaskRepository) {
        this.renovationTaskRepository = renovationTaskRepository;

    }

    /**
     * Adds a new renovation task to the repository
     */
    public void addRenovationTask(RenovationTask renovationTask) {
        renovationTaskRepository.save(renovationTask);
    }
}

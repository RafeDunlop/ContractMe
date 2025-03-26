package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;

import java.util.ArrayList;
import java.util.List;

@Service
public class EditTaskService {

    private final RenovationTaskRepository renovationTaskRepository;
    private final RenovationValidation renovationValidation;

    /**
     * Constructor for the service and links the repository and validator to the
     * service.
     * @param renovationTaskRepository for getting and updating user details
     * @param renovationValidation for validating updated task details
     */
    @Autowired
    public EditTaskService(RenovationTaskRepository renovationTaskRepository, RenovationValidation renovationValidation) {
        this.renovationTaskRepository = renovationTaskRepository;
        this.renovationValidation = renovationValidation;
    }


    public void updateTask(RenovationTaskDTO renovationTaskDTO, RenovationTask renovationTask) throws IllegalArgumentException {
        if (renovationTaskDTO == null) {
            throw new IllegalArgumentException("Data integration error");
        }

        // Validates updated user details and returns all errors
        List<String> errors = renovationValidation.validateTaskDetails(renovationTaskDTO);

        // Throws all errors that were found
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        renovationTask.setName(renovationTaskDTO.getName());
        renovationTask.setDescription(renovationTaskDTO.getDescription());
        renovationTask.setDueDate(renovationTaskDTO.getDueDate());
        renovationTask.setRoomList(renovationTaskDTO.getRooms());

        renovationTaskRepository.save(renovationTask);


    }


}

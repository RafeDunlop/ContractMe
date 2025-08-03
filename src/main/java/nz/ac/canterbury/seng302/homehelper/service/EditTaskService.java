package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.TaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class EditTaskService {

    private final RenovationTaskRepository renovationTaskRepository;
    private final RenovationTaskValidation renovationTaskValidation;

    /**
     * Constructor for the service and links the repository and validator to the
     * service.
     * @param renovationTaskRepository for getting and updating user details
     * @param renovationTaskValidation for validating updated task details
     */
    @Autowired
    public EditTaskService(RenovationTaskRepository renovationTaskRepository, RenovationTaskValidation renovationTaskValidation) {
        this.renovationTaskRepository = renovationTaskRepository;
        this.renovationTaskValidation = renovationTaskValidation;
    }

    /**
     * Updates the details of an existing renovation task based on the provided {@link RenovationTaskDTO}.
     *
     * @param renovationTaskDTO The data transfer object containing updated task details.
     * @param renovationTask    The existing renovation task to be updated.
     * @throws IllegalArgumentException If the provided DTO is null or contains validation errors.
     */
    public void updateTask(RenovationTaskDTO renovationTaskDTO, RenovationTask renovationTask) throws IllegalArgumentException {
        if (renovationTaskDTO == null) {
            throw new IllegalArgumentException("Data integration error");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        renovationTask.setName(renovationTaskDTO.getName());
        renovationTask.setDescription(renovationTaskDTO.getDescription());
        renovationTask.setDueDate(null);
        if (renovationTaskDTO.getDueDate() != null && !renovationTaskDTO.getDueDate().isBlank()) {
            renovationTask.setDueDate(LocalDate.parse(renovationTaskDTO.getDueDate(), formatter));
        }
        renovationTask.setRoomList(renovationTaskDTO.getRooms());

        renovationTaskRepository.save(renovationTask);
    }

    /**
     * Updates the icon of a renovation task with the given file name.
     *
     * @param renovationTask The renovation task to be updated.
     * @param iconFileName The file name of the new icon.
     * @throws IllegalArgumentException If the file does not exist or is a directory.
     */
    public void updateTaskIcon(RenovationTask renovationTask, String iconFileName) throws IllegalArgumentException {
        if (renovationTaskValidation.validateTaskIconFileName(iconFileName)) {
            renovationTask.setIconFileName(iconFileName);
            renovationTaskRepository.save(renovationTask);
        } else {
            throw new IllegalArgumentException("File does not exist");
        }
    }

    /**
     * Updates the state of the given renovation task.
     * @param renovationTask The task to be updated.
     * @param stateName The name of the state to update the task to.
     */
    public void updateTaskState(RenovationTask renovationTask, String stateName) {
        String error = renovationTaskValidation.validateStateName(stateName);

        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        renovationTask.setState(TaskState.valueOf(stateName));
        renovationTaskRepository.save(renovationTask);
    }
}

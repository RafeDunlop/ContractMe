package nz.ac.canterbury.seng302.homehelper.controller;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.*;
import nz.ac.canterbury.seng302.homehelper.validation.RenovationTaskValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for the edit task page
 */
@Controller
public class EditTaskController {

    private static final Logger logger = LoggerFactory.getLogger(EditTaskController.class);
    private final RenovationTaskService renovationTaskService;
    private final EditTaskService editTaskService;
    private final RenovationRecordService renovationRecordService;
    private final RenovationTaskValidation renovationTaskValidation;
    private final RenovationTaskRepository renovationTaskRepository;
    private final LoginService loginService;

    /**
     * Constructs an {@code EditTaskController} with the specified services and repository.
     *
     * @param renovationTaskService       the service for managing renovation tasks
     * @param renovationRecordService     the service for managing renovation records
     * @param editTaskService             the service handling logic specific to editing tasks
     * @param renovationTaskValidation        the validation utility for renovation-related input
     * @param renovationTaskRepository    the repository for accessing renovation task data
     * @param loginService               the service for handling logging users in
     */
    @Autowired
    public EditTaskController(RenovationTaskService renovationTaskService, RenovationRecordService renovationRecordService,
                              EditTaskService editTaskService,
                              RenovationTaskValidation renovationTaskValidation,
                              RenovationTaskRepository renovationTaskRepository,
                              LoginService loginService) {
        this.renovationTaskService = renovationTaskService;
        this.renovationRecordService = renovationRecordService;
        this.editTaskService = editTaskService;
        this.renovationTaskValidation = renovationTaskValidation;
        this.renovationTaskRepository = renovationTaskRepository;
        this.loginService = loginService;
    }

    /**
     * Gets the Renovation Task editing form
     * @param taskId The id of the Renovation Task to be edited
     * @param renovationId The id of the Renovation
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     * with values being set to relevant parameters provided
     * @return Thymeleaf editRenovationTemplate
     */
    @GetMapping("/editTask")
    public String editTask(@RequestParam(name = "taskId") Long taskId,
                           @RequestParam(name = "renovationId") Long renovationId,
                           Model model) {

        logger.info("GET renovations/editTask");

        RenovationRecord renovationRecord = renovationRecordService.getRecordById(renovationId);

        User user = loginService.getUserByEmail();
        if (renovationRecord.getUser() != user) {
            return "redirect:/main";
        }

        Optional<RenovationTask> renovationTask = renovationTaskRepository.findById(taskId);
        if (renovationTask.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation does not exist");
        }

        RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO(renovationTask.get());

        model.addAttribute("renovation", renovationRecord);
        model.addAttribute("task", renovationTask.get());
        model.addAttribute("roomList", renovationRecord.getRooms());
        model.addAttribute("renovationTaskDTO", renovationTaskDTO);

        return "editTaskTemplate";
    }


    /**
     * Handles the submission of the renovation task editing form.
     * Attempts to update an existing renovation task using the provided form data.
     * If the update is successful, it redirects to the renovation view page.
     * If an error occurs during task editing, it redirects back to the task editing page
     * with error messages and previously entered form data.
     *
     * @param renovationTaskDTO The data transfer object containing updated form data for the renovation task.
     * @param taskId The ID of the renovation task to be updated.
     * @param renovationId The ID of the renovation record associated with this task.
     * @param redirectAttributes Flash attributes used to pass data across the redirect in case of form submission errors.
     * @return A redirect string to either the renovation view page on success or back to the edit task page on failure.
     */
    @PostMapping("/editTask")
    public String editTask(@ModelAttribute("renovationTaskDTO") RenovationTaskDTO renovationTaskDTO,
                                @RequestParam(name = "taskId") Long taskId,
                                @RequestParam(name = "renovationId") Long renovationId,
                                RedirectAttributes redirectAttributes) {
        logger.info("POST renovations/editTask");

        RenovationTask renovationTask = renovationTaskService.getTaskById(taskId);

        if (renovationTaskDTO.getDueDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDueDate = renovationTaskDTO.getDueDate().format(formatter);
            redirectAttributes.addFlashAttribute("dueDate", formattedDueDate);
        }

        Map<String, List<String>> errors = renovationTaskService.validateTaskDetails(renovationTaskDTO);

        if (!errors.isEmpty()) {
            errors.forEach((key, messages) -> redirectAttributes.addFlashAttribute(key, messages));

            redirectAttributes.addFlashAttribute("renovationTaskDTO", renovationTaskDTO);
            return "redirect:/editTask?taskId=" + taskId + "&renovationId=" + renovationId;
        }

        try {
            editTaskService.updateTask(renovationTaskDTO,renovationTask);
            return "redirect:/renovations/view?id=" + renovationId;
        } catch (IllegalArgumentException e) {
            logger.warn("Form submission error {}", e.getMessage());

            List<String> errorsList = List.of(e.getMessage().split(";"));
            redirectAttributes.addFlashAttribute("errorMessages", errorsList);
            redirectAttributes.addFlashAttribute("renovationTaskDTO", renovationTaskDTO);

            return "redirect:/editTask?taskId=" + taskId + "&renovationId=" + renovationId;
        }
    }

    /**
     * Handles the submission of a request to update the icon for a specific renovation task.
     * Retrieves the renovation task by its ID and updates its icon using the provided icon name.
     * After a successful update, it redirects to the associated renovation view page.
     *
     * @param id The ID of the renovation task whose icon is being updated.
     * @param requestBody A map containing the new icon name under the key "iconName".
     * @return A redirect string to the renovation view page associated with the updated task.
     */
    @PostMapping("/editTask/edit-icon/{id}")
    public String editTaskIcon(@PathVariable("id") Long id, @RequestBody Map<String, String> requestBody) {
        logger.info("POST editTask/edit-icon/{id}");
        String iconName = requestBody.get("iconName");
        RenovationTask renovationTask = renovationTaskService.getTaskById(id);
        User user = loginService.getUserByEmail();
        if (!renovationTask.getRenovationRecord().getUser().equals(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Action not allowed.");
        }
        RenovationRecord renovation = renovationTask.getRenovationRecord();
        editTaskService.updateTaskIcon(renovationTask, iconName);
        return "redirect:/renovations/view?id=" + renovation.getId();
    }
}

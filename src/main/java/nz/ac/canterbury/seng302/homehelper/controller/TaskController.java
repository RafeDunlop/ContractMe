package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.TaskService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Controller for the create new task page
 */
@Controller
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    private final RenovationRecordService renovationRecordService;

    private final RenovationTaskRepository renovationTaskRepository;

    private final LoginService loginService;


    /**
     * Induces spring to automatically sets up the {@code RenovationRecordService}
     *
     * @param renovationRecordService The service associated with renovation records
     * @param taskService   the service layer responsible for renovation tasks
     * @param renovationTaskValidation The validation for validating tasks
     * @param loginService             the service for handling logging users in
     */
    @Autowired
    public TaskController(RenovationRecordService renovationRecordService,
                          TaskService taskService,
                          RenovationTaskRepository renovationTaskRepository,
                          LoginService loginService) {
        this.renovationRecordService = renovationRecordService;
        this.taskService = taskService;
        this.loginService = loginService;
        this.renovationTaskRepository = renovationTaskRepository;
    }

    /**
     * Gets the renovation task creation form
     *
     * @param id    Renovation record id
     * @param model Representations of params for use in thymeleaf
     * @return the create task HTML page
     */
    @GetMapping("renovations/view/create")
    public String createTask(@RequestParam(name = "id") Long id, Model model) {
        logger.info("GET renovations/view/create");

        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        }

        User user = loginService.getUserByEmail();
        if (renovationRecord.getUser() != user) {
            return "redirect:/main";
        }

        model.addAttribute("renovation", renovationRecord);
        model.addAttribute("roomList", renovationRecord.getRooms());

        if (!model.containsAttribute("renovationTaskDTO")) {
            RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO("", "", null, new ArrayList<>());
            String dueDate = renovationTaskDTO.getDueDate();
            String formattedDate = (dueDate != null) ? dueDate : "";
            model.addAttribute("renovationTaskDTO", renovationTaskDTO);
            model.addAttribute("dueDate", formattedDate);
        }

        return "createTaskTemplate";
    }



    /**
     * Handles the submission of the renovation task creation form.
     * Attempt to create a new renovation task using the provided form data.
     * If successful, it redirects to the renovation view page.
     * If an error occurs during task creation, it redirects back to the task creation page
     * with error messages and previously entered form data.
     *
     * @param renovationTaskDTO  The data transfer object containing form data for the new renovation task.
     * @param roomList           The list of rooms selected from the form. If null, defaults to all rooms from the renovation record.
     * @param renovationId       The ID of the renovation record this task is associated with.
     * @param redirectAttributes Flash attributes used to pass data across the redirect in case of form submission errors.
     * @return A redirect string to either the renovation view page on success or back to the create task page on failure.
     */
    @PostMapping("renovations/view/create")
    public String submitNewTask(@ModelAttribute("renovationTaskDTO") RenovationTaskDTO renovationTaskDTO,
                                @RequestParam(name = "roomList", required = false) List<String> roomList,
                                @RequestParam(name = "renovationId") Long renovationId,
                                RedirectAttributes redirectAttributes) {
        logger.info("POST renovations/view/create");
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(renovationId);
        if (Objects.equals(renovationTaskDTO.getDueDate(), "")) {
            renovationTaskDTO.setDueDate(null);
        }

        LocalDate parsedDate = null;

        if (renovationTaskDTO.getDueDate() != null) {
            DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    parsedDate = LocalDate.parse(renovationTaskDTO.getDueDate(), formatter);
                    break;
                } catch (DateTimeParseException ignored) {}
            }

            if (parsedDate != null) {
                String formattedDueDate = parsedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                redirectAttributes.addFlashAttribute("dueDate", formattedDueDate);
                renovationTaskDTO.setDueDate(formattedDueDate);
            }
        }

        Map<String, List<String>> errors = taskService.validateTaskDetails(renovationTaskDTO, renovationRecord);


        if (!errors.isEmpty()) {
            errors.forEach((key, messages) -> redirectAttributes.addFlashAttribute(key, messages));

            redirectAttributes.addFlashAttribute("renovationTaskDTO", renovationTaskDTO);
            redirectAttributes.addFlashAttribute("roomList", roomList);
            redirectAttributes.addFlashAttribute("renovationId", renovationId);

            return "redirect:/renovations/view/create?id=" + renovationId;
        }

        if (roomList == null) {
            roomList = renovationRecord.getRooms();
        }

        taskService.addRenovationTask(renovationTaskDTO, renovationRecord);

        return "redirect:/renovations/view?id=" + renovationId;
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
        if (renovationRecord == null || renovationRecord.getUser() != user) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation task does not exist");
        }

        Optional<RenovationTask> renovationTask = renovationTaskRepository.findById(taskId);
        if (renovationTask.isEmpty() || renovationTask.get().getRenovationRecord() != renovationRecord) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation task does not exist");
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
        RenovationTask renovationTask = taskService.getTaskById(taskId);
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(renovationId);
        LocalDate parsedDate = null;
        if (renovationRecord == null || renovationRecord.getUser() != loginService.getUserByEmail()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation record was not found.");
        }
        if (renovationTask == null || renovationTask.getRenovationRecord() != renovationRecord) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation task does not exist.");
        }
        if (renovationTaskDTO.getDueDate() != null) {
            DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    parsedDate = LocalDate.parse(renovationTaskDTO.getDueDate(), formatter);
                    break;
                } catch (DateTimeParseException ignored) {}
            }

            if (parsedDate != null) {
                String formattedDueDate = parsedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                redirectAttributes.addFlashAttribute("dueDate", formattedDueDate);
                renovationTaskDTO.setDueDate(formattedDueDate);
            }
        }

        Map<String, List<String>> errors = taskService.validateTaskDetails(
                renovationTaskDTO, renovationTask.getRenovationRecord());

        if (!errors.isEmpty()) {
            errors.forEach((key, messages) -> redirectAttributes.addFlashAttribute(key, messages));

            redirectAttributes.addFlashAttribute("renovationTaskDTO", renovationTaskDTO);
            return "redirect:/editTask?taskId=" + taskId + "&renovationId=" + renovationId;
        }

        try {
            taskService.updateTask(renovationTaskDTO,renovationTask);
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
        RenovationTask renovationTask = taskService.getTaskById(id);
        User user = loginService.getUserByEmail();
        if (!renovationTask.getRenovationRecord().getUser().equals(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Action not allowed.");
        }
        RenovationRecord renovation = renovationTask.getRenovationRecord();
        taskService.updateTaskIcon(renovationTask, iconName);
        return "redirect:/renovations/view?id=" + renovation.getId();
    }
}

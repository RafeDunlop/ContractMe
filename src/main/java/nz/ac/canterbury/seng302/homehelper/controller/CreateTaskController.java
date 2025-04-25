package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the create new task page
 */
@Controller
public class CreateTaskController {

    private static final Logger logger = LoggerFactory.getLogger(CreateTaskController.class);

    private final RenovationTaskService renovationTaskService;

    private final RenovationRecordService renovationRecordService;



    /**
     * Induces spring to automatically sets up the {@code RenovationRecordService}
     * @param renovationRecordService The service associated with renovation records
     * @param renovationTaskService the service layer responsible for renovation tasks
     *
     */
    @Autowired
    public CreateTaskController(RenovationRecordService renovationRecordService, RenovationTaskService renovationTaskService) {
        this.renovationRecordService = renovationRecordService;
        this.renovationTaskService = renovationTaskService;

    }

    /**
     * Gets the renovation task creation form
     * @param id Renovation record id
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

        model.addAttribute("renovation", renovationRecord);
        model.addAttribute("roomList", renovationRecord.getRooms());
        if (!model.containsAttribute("renovationTaskDTO")) {
            model.addAttribute("renovationTaskDTO", new RenovationTaskDTO("", "", null, new ArrayList<>()));
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
     * @param renovationTaskDTO The data transfer object containing form data for the new renovation task.
     * @param roomList The list of rooms selected from the form. If null, defaults to all rooms from the renovation record.
     * @param renovationId The ID of the renovation record this task is associated with.
     * @param redirectAttributes Flash attributes used to pass data across the redirect in case of form submission errors.
     * @return A redirect string to either the renovation view page on success or back to the create task page on failure.
     */
    @PostMapping("renovations/view/create")
    public String submitNewTask(@ModelAttribute("renovationTaskDTO") RenovationTaskDTO renovationTaskDTO,
                                @RequestParam(name = "roomList", required=false) List<String> roomList,
                                @RequestParam(name = "renovationId") Long renovationId,
                                RedirectAttributes redirectAttributes) {
        logger.info("POST renovations/view/create");
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(renovationId);

        try {
            if (roomList == null) {
                roomList = renovationRecord.getRooms();
            }

            renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord);

            return "redirect:/renovations/view?id=" + renovationId;

        } catch (IllegalArgumentException e) {
            logger.warn("Form submission error {}", e.getMessage());

            List<String> errorsList = List.of(e.getMessage().split("(?<=\\.) "));
            redirectAttributes.addFlashAttribute("errorMessages", errorsList);
            redirectAttributes.addFlashAttribute("renovationTaskDTO", renovationTaskDTO);
            redirectAttributes.addFlashAttribute("roomList", roomList);
            redirectAttributes.addFlashAttribute("renovationId", renovationId);

            return "redirect:/renovations/view/create?id=" + renovationId;
        }
    }
}

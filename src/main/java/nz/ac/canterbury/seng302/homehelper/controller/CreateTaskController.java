package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for the create new task page
 */
@Controller
public class CreateTaskController {
    Logger logger = LoggerFactory.getLogger(EditProfileController.class);


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
     * @return
     */
    @GetMapping("renovations/view/create")
    public String createTask(@RequestParam(name = "id") Long id, Model model) {
        logger.info("GET renovations/view/create");
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        model.addAttribute("renovation", renovationRecord);
        return "createTaskTemplate";
    }

    /**
     * Submits the create task form
     * @param name Submitted renovation record name
     * @param description Submitted renovation record description
     * @param roomList Submitted list of rooms associated with renovation task
     * @param dueDate Submitted task due date
     * @param model Representations of params for use in thymeleaf
     * @return
     */
    @PostMapping("renovations/view/create")
    public String submitNewTask(@RequestParam(name = "name") String name,
                                @RequestParam(name = "description") String description,
                                @RequestParam(name = "roomList", required=false) List<String> roomList,
                                @RequestParam(name = "dueDate", required=false) LocalDateTime dueDate,
                                Model model) {
        logger.info("POST renovations/view/create");

        //Necessary validation to be implemented here

        try {
            RenovationTask renovationTask = new RenovationTask(name, description, roomList, dueDate);
            renovationTaskService.addRenovationTask(renovationTask);
            model.addAttribute("task", renovationTask);
            return "viewRenovation";
        } catch (IllegalArgumentException e) {
            logger.warn("Form submission error", e);
            model.addAttribute("name", name);
            model.addAttribute("description", description);
            model.addAttribute("roomList", roomList);
            model.addAttribute("dueDate", dueDate);
            model.addAttribute("errorMessage", "Invalid input: " + e.getMessage());
            return "createTaskTemplate";
        }


    }
}

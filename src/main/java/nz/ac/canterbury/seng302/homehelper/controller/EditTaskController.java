package nz.ac.canterbury.seng302.homehelper.controller;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.service.*;
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

import java.util.List;

/**
 * Controller for the edit task page
 */
@Controller
public class EditTaskController {

    private static final Logger logger = LoggerFactory.getLogger(EditTaskController.class);

    private final RenovationTaskService renovationTaskService;

    private final EditTaskService editTaskService;

    private final RenovationRecordService renovationRecordService;

    @Autowired
    public EditTaskController(RenovationTaskService renovationTaskService,RenovationRecordService renovationRecordService,EditTaskService editTaskService) {
        this.renovationTaskService = renovationTaskService;
        this.renovationRecordService = renovationRecordService;
        this.editTaskService = editTaskService;
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
    public String editTask(@RequestParam(name = "taskId") Long taskId, @RequestParam(name = "renovationId") Long renovationId,Model model) {
        logger.info("GET renovations/editTask");
        RenovationTask renovationTask = renovationTaskService.getTaskById(taskId);
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(renovationId);
        if (renovationTask == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        model.addAttribute("renovation", renovationRecord);
        model.addAttribute("task",renovationTask);
        model.addAttribute("roomList", renovationRecord.getRooms());
        logger.info("Due date of task: {}", renovationTask.getDueDate());
        model.addAttribute("renovationTaskDTO", new RenovationTaskDTO(renovationTask.getName(),renovationTask.getDescription(),renovationTask.getDueDate(),renovationTask.getRoomList()));
        return "editTaskTemplate";
    }


    /**
     * Submits the create task form
     * @param model Representations of params for use in thymeleaf
     * @return either view renovation or create task pages
     */

    @PostMapping("/editTask")
    public String editTask(@ModelAttribute("renovationTaskDTO") RenovationTaskDTO renovationTaskDTO,
                                @RequestParam(name = "taskId") Long taskId,
                                @RequestParam(name = "renovationId") Long renovationId,
                                Model model) {
        logger.info("POST renovations/view/create");

        RenovationTask renovationTask = renovationTaskService.getTaskById(taskId);
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(renovationId);

        try {

            editTaskService.updateTask(renovationTaskDTO,renovationTask);

            model.addAttribute("renovation", renovationRecord);

            return "redirect:/renovations/view?id=" + renovationId;

        } catch (IllegalArgumentException e) {
            logger.warn("Form submission error {}", e.getMessage());

            List<String> errorsList = List.of(e.getMessage().split("(?<=\\.) "));
            model.addAttribute("errorMessages", errorsList);

            model.addAttribute("renovationTaskDTO", renovationTaskDTO);
            model.addAttribute("id", renovationId);
            model.addAttribute("task",renovationTask);
            model.addAttribute("renovation", renovationRecord);
            model.addAttribute("roomList", renovationRecord.getRooms());
            return "editTaskTemplate";
        }
    }
}

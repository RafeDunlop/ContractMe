package nz.ac.canterbury.seng302.homehelper.controller;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.EditProfileService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
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

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controller for the edit task page
 */
@Controller
public class EditTaskController {
    Logger logger = LoggerFactory.getLogger(EditProfileController.class);
    private final RenovationTaskService renovationTaskService;

    private final RenovationRecordService renovationRecordService;

    public EditTaskController(RenovationTaskService renovationTaskService,RenovationRecordService renovationRecordService) {
        this.renovationTaskService = renovationTaskService;
        this.renovationRecordService = renovationRecordService;
    }

    /**
     * Gets the Renovation Task editing form
     * @param taskId The id of the Renovation Task to be edited
     * @param renoId The id of the Renovation
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     * with values being set to relevant parameters provided
     * @return Thymeleaf editRenovationTemplate
     */
    @GetMapping("/editTask")
    public String editTask(@RequestParam(name = "taskId") Long taskId, @RequestParam(name = "renoId") Long renoId,Model model) {
        logger.info("GET renovations/editTask");
        RenovationTask renovationTask = renovationTaskService.getTaskdById(taskId);
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(renoId);
        if (renovationTask == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        model.addAttribute("renovation", renovationRecord);
        model.addAttribute("task",renovationTask);
        model.addAttribute("roomList", renovationRecord.getRooms());
        model.addAttribute("renovationTaskDTO", new RenovationTaskDTO("","",null));
        return "editTaskTemplate";
    }
}

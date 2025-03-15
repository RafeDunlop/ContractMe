package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
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

/**
 * Controller for the create new task page
 */
@Controller
public class CreateTaskController {
    Logger logger = LoggerFactory.getLogger(EditProfileController.class);


    private final RenovationRecordService renovationRecordService;

    @Autowired
    public CreateTaskController(RenovationRecordService renovationRecordService) {
        this.renovationRecordService = renovationRecordService;
    }


    @GetMapping("renovations/view/create")
    public String createTask(@RequestParam(name = "id") Long id, Model model) {
        logger.info("GET renovations/view/create");
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        model.addAttribute("renovation", renovationRecord);
        return "createTaskTemplate";

    }

    @PostMapping("renovations/view/create")
    public String submitNewTask(Model model) {
        logger.info("POST renovations/view/create");


        return "createTaskTemplate";

    }
}

package nz.ac.canterbury.seng302.homehelper.controller;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.EditProfileService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
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


    /**
     * Gets the Renovation Task editing form
     * @param id The id of the Renovation Task to be edited
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     * with values being set to relevant parameters provided
     * @return Thymeleaf editRenovationTemplate
     */
    @GetMapping("/editTask")
    public String editTask(@RequestParam(name = "id") Long id, Model model) {

        return "editTaskTemplate";
    }
}

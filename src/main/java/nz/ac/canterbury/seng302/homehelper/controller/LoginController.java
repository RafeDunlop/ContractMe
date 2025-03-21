package nz.ac.canterbury.seng302.homehelper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for the login page.
 */
@Controller
public class LoginController {
    Logger logger = LoggerFactory.getLogger(LoginController.class);

    /**
     * Handler for a get request to the login page.
     * @return loginTemplate
     */
    @GetMapping("/login")
    public String login(@RequestParam(value="error", required = false) String error, Model model) {
        logger.info("GET /login");
        if (error != null) {
            List<String> errorsList = List.of(error.split("(?<=\\.) "));
            model.addAttribute("errorMessage", errorsList);
        }
        return "loginTemplate";
    }
}

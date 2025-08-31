package nz.ac.canterbury.seng302.homehelper.controller;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the main page.
 */

@Controller
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final LoginService loginService;

    public MainController(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * Handler for a get request to the main page.
     *
     * @return mainTemplate
     */
    @GetMapping("/main")
    public String login(Model model, HttpSession session) {
        logger.info("GET /Main");
        User user = loginService.getUserByEmail();
        session.setAttribute("isContractor",user instanceof Contractor contractor);
        return "mainTemplate";
    }
}
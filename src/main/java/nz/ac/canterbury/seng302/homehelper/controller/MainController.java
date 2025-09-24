package nz.ac.canterbury.seng302.homehelper.controller;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the main page.
 */

@Controller
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    /**
     * Handler for a get request to the main page.
     *
     * @return mainTemplate
     */
    @GetMapping("/main")
    public String login(HttpServletRequest request) {
        logger.info("GET /Main");

        request.getSession().setAttribute("lastVisitedRenovationPage", "/main");
        request.getSession().setAttribute("lastVisitedRenovationParameters", "");

        return "mainTemplate";
    }
}
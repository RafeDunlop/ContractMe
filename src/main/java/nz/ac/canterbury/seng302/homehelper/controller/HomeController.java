package nz.ac.canterbury.seng302.homehelper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the home page.
 */
@Controller
public class HomeController {
    Logger logger = LoggerFactory.getLogger(HomeController.class);

    /**
     * Redirects GET default url '/' to '/home'
     * @return redirect to /home
     */
    @GetMapping("/")
    public String home() {
        logger.info("GET /");
        return "homeTemplate";
    }

    /**
     * Gets the thymeleaf homepage
     * @return thymeleaf homepageTemplate
     */
    @GetMapping("/home")
    public String getHomepage() {
        logger.info("GET /home");
        return "homeTemplate";
    }
}

package nz.ac.canterbury.seng302.homehelper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * Controller for the login page.
 */
@Controller
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    /**
     * Handler for a get request to the login page.
     * @return loginTemplate
     */
    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        logger.info("GET /login");

        Object error = request.getSession().getAttribute("errorMessage");
        if (error != null) {
            List<String> errorsList = List.of(error.toString().split("(?<=\\.) "));
            model.addAttribute("errorMessage", errorsList);
            request.getSession().removeAttribute("errorMessage");
        }

        return "loginTemplate";
    }
}

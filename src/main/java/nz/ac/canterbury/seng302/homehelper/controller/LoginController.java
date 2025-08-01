package nz.ac.canterbury.seng302.homehelper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

        Object email = request.getSession().getAttribute("email");
        model.addAttribute("email", email);



        model.addAttribute("profilePicture", "/profile_pictures/default/default.jpg");
        request.getSession().removeAttribute("email");

        Object error = request.getSession().getAttribute("errorMessage");
        if (error != null) {
            List<String> errorsList = List.of(error.toString().split(";"));

            List<String> emailErrors = new ArrayList<>();
            List<String> generalErrors = new ArrayList<>();

            for (String err : errorsList) {
                if (err.trim().toLowerCase().contains("email address must be in the form")) {
                    emailErrors.add(err.trim());
                } else {
                    generalErrors.add(err.trim());
                }
            }

            if (!emailErrors.isEmpty()) {
                model.addAttribute("emailError", emailErrors);
            }

            if (!generalErrors.isEmpty()) {
                model.addAttribute("errorMessage", generalErrors);
            }

            request.getSession().removeAttribute("errorMessage");
        }

        return "loginTemplate";
    }


    private void getDefaultProfilePicture() {
        Paths.get("profile_pictures/default").resolve("default.jpg").normalize();
    }
}

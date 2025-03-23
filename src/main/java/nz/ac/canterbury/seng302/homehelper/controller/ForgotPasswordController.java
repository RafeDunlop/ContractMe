package nz.ac.canterbury.seng302.homehelper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ForgotPasswordController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);

    @GetMapping("/password/forgot")
    public String forgotPassword() {
        logger.info("GET /forgot-password");
        return "forgotPasswordTemplate";
    }

    @PostMapping("/password/forgot")
    public String submitEmail(Model model) {
        logger.info("POST /forgot-password");
        model.addAttribute("emailMessage", "An email was sent to the address if it was recognised");
        return "forgotPasswordTemplate";
    }
}

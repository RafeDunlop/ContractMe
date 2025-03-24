package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.service.ForgotPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotPasswordController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);
    ForgotPasswordService forgotPasswordService;

    @Autowired
    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }

    @GetMapping("/password/forgot")
    public String forgotPassword() {
        logger.info("GET /forgot-password");
        return "forgotPasswordTemplate";
    }

    @PostMapping("/password/forgot")
    public String submitEmail(@RequestParam("email") String email, Model model) {
        logger.info("POST /forgot-password");
        String errorMessage = forgotPasswordService.validateEmail(email);
        System.out.println(errorMessage);
        if (errorMessage.isEmpty()) {
            model.addAttribute("emailMessage", "An email was sent to the address if it was recognised");
        } else {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "forgotPasswordTemplate";
    }
}

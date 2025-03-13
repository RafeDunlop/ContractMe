package nz.ac.canterbury.seng302.homehelper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ChangePasswordController {
    Logger logger = LoggerFactory.getLogger(RegisterController.class);


    @GetMapping("/change-password")
    public String changePassword() {
        logger.info("GET /change-password/");

        return "updatePasswordTemplate";
    }

    @PostMapping("/change-password")
    public String tryChangePassword() {

        return "mainTemplate";
    }

}

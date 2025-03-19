package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user/activate")
public class SignupCodeController {

    Logger logger = LoggerFactory.getLogger(SignupCodeController.class);

    VerificationCodeService verificationCodeService;

    @Autowired
    public SignupCodeController(VerificationCodeService verificationCodeService) {
        this.verificationCodeService = verificationCodeService;
    }

    @GetMapping
    public String getAccountActivationForm(@RequestParam(name = "email") String email,
                                           @RequestParam(name = "code", required = false, defaultValue = "") String code,
                                           @RequestParam(name = "errorMessage", required = false) String errorMessage,
                                           Model model) {
        logger.info("GET /user/activate");
        model.addAttribute("email", email);
        model.addAttribute("code", code);
        model.addAttribute("errorMessage", errorMessage); //ensure error messages persist
        return "signupCodeTemplate";
    }

    @PostMapping
    public String activateAccount(@RequestParam(name = "code") String code, Model model) {
        try {
            verificationCodeService.consumeSignupCode(code);
            //todo: add handling for the message indicated in U6 AC5
            return "loginTemplate";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("code", code);
            return "signupCodeTemplate";
        }
    }
}

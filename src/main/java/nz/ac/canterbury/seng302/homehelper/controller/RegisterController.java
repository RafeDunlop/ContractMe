package nz.ac.canterbury.seng302.homehelper.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.event.OnRegistrationCompleteEvent;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller for the registration page
 */
@Controller
public class RegisterController {
    Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final RegisterService registerService;
    private final VerificationCodeService verificationCodeService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor for the register class, links controller and service layers
     */
    @Autowired
    public RegisterController(RegisterService registerService,
            ApplicationEventPublisher eventPublisher,
            VerificationCodeService verificationCodeService) {
        this.registerService = registerService;
        this.verificationCodeService = verificationCodeService;
        this.eventPublisher = eventPublisher;
    }


    /**
     * Method to display the registration page under the path /register
     *
     * @return thymeleaf registration
     * @ModelAttribute userRegisterDTO, contains all params needed for a user object
     */
    @GetMapping("/register")
    public String registration(@ModelAttribute UserRegisterDTO userRegisterDTO) {
        logger.info("GET /register");
        return "registrationTemplate";
    }

    /**
     * Posts a form response with user details
     *
     * @return thymeleaf registration
     * @ModelAttribute userRegisterDTO, contains all params needed for a user object
     */
    @PostMapping("/register")
    public String submitRegistration(@ModelAttribute UserRegisterDTO userRegisterDTO,
                                     Model model, HttpServletRequest request) {
        logger.info("POST /register");
        try {
            User user = registerService.registerUser(userRegisterDTO);
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale()));
            return "redirect:/confirm-registration";
        } catch (IllegalArgumentException|MailException e) {
            logger.warn("Form submission error: " + e.getMessage());

            List<String> errorsList = List.of(e.getMessage().split("(?<=\\.) "));

            model.addAttribute("errorMessages", errorsList);

            model.addAttribute("firstName", userRegisterDTO.getFirstName().trim());
            model.addAttribute("lastName", userRegisterDTO.getLastName().trim());
            model.addAttribute("email", userRegisterDTO.getEmail().trim());
        }
        return "registrationTemplate";
    }

    /**
     * Get mapping for the email verification code form.
     */
    @GetMapping("/confirm-registration")
    public String confirmRegistration() {
        logger.info("GET /confirm-registration");
        return "emailVerificationForm";
    }

    /**
     * Post mapping to verify the code and grant the user the "USER" role.
     *
     * @param code the verification code
     */
    @PostMapping("/confirm-registration")
    public String verifyRegistration(@RequestParam String code, Model model) {
        logger.info("POST /confirm-registration code: {}", code);
        try {
            verificationCodeService.consumeSignupCode(code);
        } catch (IllegalArgumentException error) {
            model.addAttribute("errorMessage", error.getMessage());
            return "emailVerificationForm";
        }
        return "redirect:/login";
    }

}

package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.event.OnRegistrationCompleteEvent;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;


/**
 * Controller for the registration page
 */
@Controller
public class RegisterController {
    Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final RegisterService registerService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor for the register class, links controller and service layers
     */
    @Autowired
    public RegisterController(RegisterService registerService, ApplicationEventPublisher eventPublisher) {
        this.registerService = registerService;
        this.eventPublisher = eventPublisher;
    }


    /**
     * Method to display the registration page under the path /register
     *
     * @return thymeleaf registration
     * @ModelAttribute userRegisterDTO, contains all params needed for a user object
     */
    @GetMapping("/register")
    public String registration(@ModelAttribute UserRegisterDTO userRegisterDTO,
                               Model model) {
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
            registerService.authenticateUser(user, userRegisterDTO.getPassword(), request);
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale()));
            return "redirect:/user";
        } catch (IllegalArgumentException e) {
            logger.warn("Form submission error: " + e.getMessage());

            List<String> errorsList = List.of(e.getMessage().split("(?<=\\.) "));

            model.addAttribute("errorMessages", errorsList);

            model.addAttribute("firstName", userRegisterDTO.getFirstName().trim());
            model.addAttribute("lastName", userRegisterDTO.getLastName().trim());
            model.addAttribute("email", userRegisterDTO.getEmail().trim());
        } catch (RuntimeException ex) {
            logger.warn("Email send error: " + ex.getMessage());
            List<String> errorsList = List.of(ex.getMessage().split("(?<=\\.) "));
            model.addAttribute("errorMessages", errorsList);
            model.addAttribute("firstName", userRegisterDTO.getFirstName().trim());
            model.addAttribute("lastName", userRegisterDTO.getLastName().trim());
            model.addAttribute("email", userRegisterDTO.getEmail().trim());
        }
        return "registrationTemplate";
    }


}

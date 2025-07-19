package nz.ac.canterbury.seng302.homehelper.controller;

import java.util.List;
import java.util.Map;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.event.OnRegistrationCompleteEvent;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Controller for the registration page
 */
@Controller
public class RegisterController {

    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    private final RegisterService registerService;

    private final VerificationCodeService verificationCodeService;

    private final ApplicationEventPublisher eventPublisher;
    private final LocationService locationService;
    private final ContractorService contractorService;

    /**
     * Constructor for the register class, links controller and service layers
     */
    @Autowired
    public RegisterController(RegisterService registerService,
                              ApplicationEventPublisher eventPublisher,
                              VerificationCodeService verificationCodeService, LocationService locationService, ContractorService contractorService) {
        this.registerService = registerService;
        this.verificationCodeService = verificationCodeService;
        this.eventPublisher = eventPublisher;
        this.locationService = locationService;
        this.contractorService = contractorService;
    }

    /**
     * Method to display the registration page under the path /register
     *
     * @return thymeleaf registration
     * @param userRegisterDTO, contains all params needed for a user object
     * @param addressDTO, contains all params for a location object
     */
    @GetMapping("/register")
    public String registration(@ModelAttribute UserRegisterDTO userRegisterDTO,
                               @ModelAttribute AddressDTO addressDTO) {
        logger.info("GET /register");
        return "registrationTemplate";
    }

    /**
     * Handles form submission for user registration.
     *
     * @param userRegisterDTO the data transfer object containing user registration details
     * @param addressDTO, dto containing user location details
     * @param request the HTTP servlet request
     * @param redirectAttributes attributes for a redirect scenario
     * @return redirect address
     */
    @PostMapping("/register")
    public String submitRegistration(@ModelAttribute UserRegisterDTO userRegisterDTO,
                                     @ModelAttribute AddressDTO addressDTO,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        logger.info("POST /register");

        Map<String, List<String>> errors = registerService.validateRegistration(userRegisterDTO);


        boolean locationProvided = locationService.isLocationProvided(addressDTO);
        if (locationProvided) {
            errors.putAll(locationService.validateLocation(addressDTO));
        }

        if (userRegisterDTO.getIsContractor()) {
            errors.putAll(contractorService.validateContractor(userRegisterDTO));
        }

        if (!errors.isEmpty()) {
            errors.forEach(redirectAttributes::addFlashAttribute);
            redirectAttributes.addFlashAttribute("userRegisterDTO", userRegisterDTO);
            redirectAttributes.addFlashAttribute("addressDTO", addressDTO);
            redirectAttributes.addFlashAttribute("locationUsed", locationProvided);
            return "redirect:/register";
        }

        try {
            User user;
            if (userRegisterDTO.getIsContractor()) {
                user = contractorService.registerContractor(userRegisterDTO, addressDTO);
            } else {
                user = registerService.registerUser(userRegisterDTO);
                if (locationService.isLocationProvided(addressDTO)) {
                    registerService.registerLocation(user, addressDTO);
                }
            }
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user, request.getLocale()));
            return "redirect:/confirm-registration";
        } catch (MailException e) {
            redirectAttributes.addFlashAttribute("error", "Error sending confirmation email.");
            redirectAttributes.addFlashAttribute("userRegisterDTO", userRegisterDTO);
            redirectAttributes.addFlashAttribute("addressDTO", addressDTO);
            redirectAttributes.addFlashAttribute("locationUsed", locationProvided);
            return "redirect:/register";
        }
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
    public String verifyRegistration(@RequestParam(name="code") String code,
                                     RedirectAttributes redirectAttributes) {
        logger.info("POST /confirm-registration code: {}", code);
        try {
            verificationCodeService.consumeSignupCode(code);
        } catch (IllegalArgumentException error) {
            redirectAttributes.addFlashAttribute("errorMessage", error.getMessage());
            return "redirect:/confirm-registration";
        }
        redirectAttributes.addFlashAttribute("loginMessage", "Your account has been activated, please log in");
        return "redirect:/login";
    }
}

package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import nz.ac.canterbury.seng302.homehelper.service.EditProfileService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.util.LocaleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

/**
 * Controller for the edit profile page
 */
@Controller
public class EditProfileController {

    private static final Logger logger = LoggerFactory.getLogger(EditProfileController.class);
    private static final String ADDRESS_DTO = "addressDTO";
    private static final String CONTRACTOR_DTO = "contractorDTO";
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String EMAIL = "email";
    private static final String PROFILE_PICTURE = "profilePicture";

    private final EditProfileService editProfileService;
    private final LoginService loginService;
    private final LocationService locationService;
    private final ContractorService contractorService;
    private final LocaleUtil localeUtil;

    /**
     * Constructor for the controller and links the services to the controller.
     * @param editProfileService EditProfileService for validating updated user
     *                           and updating user details in database
     * @param locationService LocationService for the user's location.
     * @param loginService LoginService for getting user by ID
     */
    @Autowired
    public EditProfileController(EditProfileService editProfileService, LoginService loginService,
                                 LocationService locationService, ContractorService contractorService) {
        this.editProfileService = editProfileService;
        this.loginService = loginService;
        this.locationService = locationService;
        this.contractorService = contractorService;
        localeUtil = new LocaleUtil();
    }

    /**
     * Displays the editProfileTemplate page under the path "/user/edit" where id
     * is the ID of the user. Sets the current user to the page.
     * @param model Model interface
     * @return editProfileTemplate page
     */
    @GetMapping("user/edit")
    public String editProfile(Model model, HttpServletRequest request) {
        logger.info("GET /user/edit");
        try {
            User user = loginService.getUserByEmail();
            if (!model.containsAttribute(FIRST_NAME)) {
                model.addAttribute(FIRST_NAME, user.getFirstName());
            }
            if (!model.containsAttribute(LAST_NAME)) {
                model.addAttribute(LAST_NAME, user.getLastName());
            }
            if (!model.containsAttribute(EMAIL)) {
                model.addAttribute(EMAIL, user.getEmail());
            }
            model.addAttribute(PROFILE_PICTURE, user.getProfilePicture());

            if (user instanceof Contractor contractor) {
                model.addAttribute("isContractor", true);
                if (!model.containsAttribute(CONTRACTOR_DTO)) {
                    UserRegisterDTO contractorDTO = new UserRegisterDTO();
                    contractorDTO.setHourlyRate(contractor.getHourlyRate());
                    contractorDTO.setPhoneNumber(contractor.getPhoneNumber());
                    contractorDTO.setCountryCode(contractor.getCountryCode());
                    contractorDTO.setSkills(new ArrayList<>(contractor.getSkills()));
                    model.addAttribute(CONTRACTOR_DTO, contractorDTO);
                    model.addAttribute("isAvailable", contractor.getAvailable());
                }
                List<Skill> skillList = Skill.listOfSortedSkills();
                model.addAttribute("skills", skillList);
                Locale locale = localeUtil.getSafeLocale(request.getLocale());
                Currency currency = Currency.getInstance(locale);
                model.addAttribute("currencySymbol", currency.getSymbol(locale));
            } else {
                model.addAttribute("isContractor", false);
            }

            // Only add addressDTO if not present from flash
            if (!model.containsAttribute(ADDRESS_DTO)) {
                AddressDTO addressDTO = new AddressDTO();
                Location location = user.getLocation();
                if (location != null) {
                    addressDTO.setFromLocation(location);
                    model.addAttribute("hasLocation", true);
                }
                model.addAttribute(ADDRESS_DTO, addressDTO);
            }
            return "editProfileTemplate";
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Handles the submission of the edit profile form.
     * Attempt to edit the profile using the provided form data.
     * If successful, it redirects to the profile view page.
     * If an error occurs during task creation, it redirects back to the edit profile
     * with error messages and previously entered form data.
     *
     * @param updatedUser The user containing the edited profile details.
     * @param addressDTO the dto containing data relating to fields in address form.
     * @param contractorDTO the dto containing data relating to the contractor
     * @param redirectAttributes Flash attributes used to pass data across the redirect in case of form submission errors.
     * @return A redirect string to either the profile view page on success or back to the edit profile page on failure.
     */
    @PostMapping("user/edit")
    public String updateProfile(@ModelAttribute User updatedUser,
                                @ModelAttribute AddressDTO addressDTO,
                                @ModelAttribute UserRegisterDTO contractorDTO,
                                RedirectAttributes redirectAttributes) {
        logger.info("POST /user/edit");

        User newUser = loginService.getUserByEmail();
        Contractor contractor = contractorService.getContractorById(newUser.getId());
        boolean sameEmail = newUser.getEmail().equals(updatedUser.getEmail());

        Map<String, List<String>> errors = editProfileService.validateUpdate(updatedUser, sameEmail);

        // Checks if the user's location has been modified in the form and compares to their old location.
        Location currentLocation = newUser.getLocation();
        errors.putAll(locationService.validateLocation(addressDTO));
        if (contractor != null) {
            errors.putAll(contractorService.validateContractor(contractorDTO, locationService.isLocationProvided(addressDTO)));
        }

        if (!errors.isEmpty()) {
            errors.forEach(redirectAttributes::addFlashAttribute);
            redirectAttributes.addFlashAttribute(FIRST_NAME, updatedUser.getFirstName());
            redirectAttributes.addFlashAttribute(LAST_NAME, updatedUser.getLastName());
            redirectAttributes.addFlashAttribute(EMAIL, updatedUser.getEmail());
            redirectAttributes.addFlashAttribute(PROFILE_PICTURE, newUser.getProfilePicture());
            redirectAttributes.addFlashAttribute(CONTRACTOR_DTO, contractorDTO);
            redirectAttributes.addFlashAttribute(ADDRESS_DTO, addressDTO);
            if (currentLocation != null || locationService.isLocationProvided(addressDTO)) {
                redirectAttributes.addFlashAttribute("hasLocation", true);
            }
            return "redirect:/user/edit";
        }

        newUser.setFirstName(updatedUser.getFirstName());
        newUser.setLastName(updatedUser.getLastName());
        newUser.setEmail(updatedUser.getEmail());
        newUser = editProfileService.updateUserLocation(newUser, addressDTO);
        if (contractor != null) {
            editProfileService.updateContractor(contractorDTO, contractor);
        } else {
            editProfileService.updateUser(newUser);
        }

        return "redirect:/user";
    }

    /**
     * Handles the upload of a profile picture for the currently logged-in user.
     * This method retrieves the currently logged-in user,
     * Stores the profile picture locally,
     * updates their profile picture using the generated address for the provided file.
     *
     * @param file  The MultipartFile representing the uploaded profile picture.
     * @param redirectAttributes used to pass errors to the view on the user/edit page.
     * @return Redirect users back to the user profile page
     */
    @PostMapping("/user/edit/profile-picture")
    public String uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        User user = loginService.getUserByEmail();
        List<String> errors = editProfileService.updateProfilePicture(user, file);

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("profilePictureError", errors);
            return "redirect:/user/edit";
        }

        session.setAttribute(PROFILE_PICTURE, user.getProfilePicture());

        return "redirect:/user";
    }
}
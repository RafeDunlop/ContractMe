package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.EditProfileService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Controller for the edit profile page
 */
@Controller
public class EditProfileController {

    private static final Logger logger = LoggerFactory.getLogger(EditProfileController.class);

    private final EditProfileService editProfileService;

    private final LoginService loginService;

    /**
     * Constructor for the controller and links the services to the controller.
     * @param editProfileService EditProfileService for validating updated user
     *                           and updating user details in database
     * @param loginService LoginService for getting user by ID
     */
    @Autowired
    public EditProfileController(EditProfileService editProfileService, LoginService loginService) {
        this.editProfileService = editProfileService;
        this.loginService = loginService;
    }

    /**
     * Displays the editProfileTemplate page under the path "/user/edit" where id
     * is the ID of the user. Sets the current user to the page.
     * @param model Model interface
     * @return editProfileTemplate page
     */
    @GetMapping("user/edit")
    public String editProfile(Model model) {
        logger.info("GET /user/edit");
        try {
            // Sets current user to page
            User user = loginService.getUserByEmail();
            model.addAttribute("user", user);
            model.addAttribute("firstName", user.getFirstName());
            model.addAttribute("lastName", user.getLastName());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("profilePicture", user.getProfilePicture());

            return "editProfileTemplate";
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Handles the submission of the edit profile form.
     * Attempt to edit profile using the provided form data.
     * If successful, it redirects to the profile view page.
     * If an error occurs during task creation, it redirects back to the edit profile.
     * with error messages and previously entered form data.
     *
     * @param updatedUser The user containing the edited profile details.
     * @param redirectAttributes Flash attributes used to pass data across the redirect in case of form submission errors.
     * @return A redirect string to either the profile view page on success or back to the edit profile page on failure.
     */
    @PostMapping("user/edit")
    public String updateProfile(@ModelAttribute User updatedUser, RedirectAttributes redirectAttributes) {
        logger.info("POST /user/edit");

        User newUser = loginService.getUserByEmail();
        boolean sameEmail = newUser.getEmail().equals(updatedUser.getEmail());

        Map<String, List<String>> errors = editProfileService.validateUpdate(updatedUser, sameEmail);

        if (!errors.isEmpty()) {
            errors.forEach((key, messages) -> redirectAttributes.addFlashAttribute(key, messages));

            redirectAttributes.addFlashAttribute("user", newUser);
            redirectAttributes.addFlashAttribute("firstName", newUser.getFirstName());
            redirectAttributes.addFlashAttribute("lastName", newUser.getLastName());
            redirectAttributes.addFlashAttribute("email", newUser.getEmail());
            redirectAttributes.addFlashAttribute("profilePicture", newUser.getProfilePicture());

            return "redirect:/user/edit";
        }

        try {
            newUser.setFirstName(updatedUser.getFirstName());
            newUser.setLastName(updatedUser.getLastName());
            newUser.setEmail(updatedUser.getEmail());

            editProfileService.updateUser(newUser);
            return "redirect:/user";
        } catch (NoSuchElementException pageNotFoundError) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, pageNotFoundError.getMessage());
        } catch (IllegalArgumentException detailsInvalidError) {
            // Set error messages on page
            logger.warn("Form submission error: {}", detailsInvalidError.getMessage());
            List<String> errorsList = List.of(detailsInvalidError.getMessage().split("(?<=\\.) "));

            redirectAttributes.addFlashAttribute("errorMessages", errorsList);
            redirectAttributes.addFlashAttribute("user", newUser); // for repopulation
            redirectAttributes.addFlashAttribute("firstName", newUser.getFirstName());
            redirectAttributes.addFlashAttribute("lastName", newUser.getLastName());
            redirectAttributes.addFlashAttribute("email", newUser.getEmail());
            redirectAttributes.addFlashAttribute("profilePicture", newUser.getProfilePicture());

            return "redirect:/user/edit";
        }
    }

    /**
     * Handles the upload of a profile picture for the currently logged-in user.
     * This method retrieves the currently logged-in user,
     * Stores the profile picture locally,
     * updates their profile picture using the generated address for the provided file,
     *
     * @param file  The MultipartFile representing the uploaded profile picture.
     * @param redirectAttributes used to pass errors to the view on the user/edit page.
     * @return Redirect users back to the user profile page
     */
    @PostMapping("/user/edit/profile-picture")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        User user = loginService.getUserByEmail();
        List<String> errors = editProfileService.updateProfilePicture(user, file);

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("profilePictureError", errors);
            return "redirect:/user/edit";
        }

        return "redirect:/user";
    }
}
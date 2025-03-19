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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controller for the edit profile page
 */
@Controller
public class EditProfileController {
    Logger logger = LoggerFactory.getLogger(EditProfileController.class);

    private final EditProfileService editProfileService;
    private final LoginService loginService;
    private final String UPLOAD_DIR = "profile_pictures/";


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
            User user = loginService.getUserByEmail();
            model.addAttribute("user", user);
            model.addAttribute("firstName", user.getFirstName());
            model.addAttribute("lastName", user.getLastName());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("profilePictureFileName", user.getProfilePicture());

            return "editProfileTemplate";
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }



    /**
     * Posts a form with the updated user details. Goes back to "/user" if the
     * user is updated; otherwise, the error messages are set and stays on same page.
     * @param updatedUser User object with the updated user details
     * @param model Model interface
     * @return editProfileTemplate page or redirect to user page
     */
    @PostMapping("user/edit")
    public String updateProfile(@ModelAttribute User updatedUser, Model model) {
        logger.info("POST /user/edit");
        User newUser = null;
        try {
            // Get original user and email, then set details to updated user values
            newUser = loginService.getUserByEmail();
            boolean sameEmail = newUser.getEmail().equals(updatedUser.getEmail());
            newUser.setFirstName(updatedUser.getFirstName());
            newUser.setLastName(updatedUser.getLastName());
            newUser.setEmail(updatedUser.getEmail());

            // Send updated user details for validation and updating
            editProfileService.updateUser(newUser, sameEmail);
            return "redirect:/user";
        } catch (NoSuchElementException pageNotFoundError) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, pageNotFoundError.getMessage());
        } catch (IllegalArgumentException detailsInvalidError) {
            // Set error messages on page
            logger.warn("Form submission error: {}", detailsInvalidError.getMessage());
            List<String> errorsList = List.of(detailsInvalidError.getMessage().split("(?<=\\.) "));
            model.addAttribute("errorMessages", errorsList);

            model.addAttribute("user", newUser);
            assert newUser != null;
            model.addAttribute("firstName", newUser.getFirstName());
            model.addAttribute("lastName", newUser.getLastName());
            model.addAttribute("email", newUser.getEmail());
            return "editProfileTemplate";
        }
    }

    /**
     * Handles the upload of a profile picture for the currently logged-in user.
     * This method retrieves the currently logged-in user,
     * Stores the profile picture locally,
     * updates their profile picture using the generated address for the provided file,
     *
     * @param file  The MultipartFile representing the uploaded profile picture.
     * @param model The Model object used to pass attributes to the view.
     * @return Redirect users back to the user profile page
     */
    @PostMapping("/user/uploadProfilePicture")
    public String uploadProfilePicture(@RequestParam("file") MultipartFile file, Model model) {
        User user = loginService.getUserByEmail();
        List<String> errors = editProfileService.updateProfilePicture(user, file);

        if (!errors.isEmpty()) {
            model.addAttribute("errorMessages", errors);
        }
        return "redirect:/user";
    }
}

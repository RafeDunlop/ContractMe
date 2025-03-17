package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.service.EditProfileService;
import nz.ac.canterbury.seng302.homehelper.service.UpdatePasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ChangePasswordController {
    Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private UpdatePasswordService updatePasswordService;

    private EditProfileService editProfileService;
    @Autowired
    public ChangePasswordController(UpdatePasswordService updatePasswordService,EditProfileService editProfileService) {
        this.updatePasswordService = updatePasswordService;
        this.editProfileService = editProfileService;
    }

    /**
     * Displays the editProfileTemplate page under the path "/user/edit" where id
     * is the ID of the user. Sets the current user to the page.
     * @param model Model interface
     * @return editProfileTemplate page
     */
    @GetMapping("user/edit/updatePassword")
    public String updatePassword(Model model) {
        model.addAttribute("updatePasswordDTO", new UpdatePasswordDTO());
        logger.info("GET /user/edit/updatePassword");
        try {
            return "updatePasswordTemplate";
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("user/edit/updatePassword")
    public String tryChangePassword(@ModelAttribute("updatePasswordDTO") UpdatePasswordDTO updatePasswordDTO, BindingResult bindingResult, Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> logger.info(error.getDefaultMessage()));
        }
        try {
            updatePasswordService.updatePassword(updatePasswordDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully.");
            return "redirect:/user";
        } catch (IllegalArgumentException e) {
            logger.warn("Form submission error: " + e.getMessage());

            List<String> errorsList = List.of(e.getMessage().split("(?<=\\.) "));

            model.addAttribute("errorMessages", errorsList);

        }
        return "updatePasswordTemplate";
    }
}

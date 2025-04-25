package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
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
public class UpdatePasswordController {

    private static final Logger logger = LoggerFactory.getLogger(UpdatePasswordController.class);
    private final UpdatePasswordService updatePasswordService;

    @Autowired
    public UpdatePasswordController(UpdatePasswordService updatePasswordService) {
        this.updatePasswordService = updatePasswordService;
    }

    /**
     * Displays the UpdatePasswordTemplate page under the path "/user/edit/updatePassword"
     * @param model Model interface
     * @return updatePassword page
     */
    @GetMapping("user/edit/updatePassword")
    public String updatePassword(Model model) {
        model.addAttribute("updatePasswordDTO", new UpdatePasswordDTO("","",""));
        logger.info("GET /user/edit/updatePassword");
        try {
            return "updatePasswordTemplate";
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Posts a form with the updated user password. Goes back to "/user" if the
     * user password is updated; otherwise, the error messages are set and stays on same page.
     * @param updatePasswordDTO User object with the updated user details
     * @param bindingResult for binding error messages
     * @param redirectAttributes to redirect the success message to /user page.
     * @return updatePasswordTemplate page or redirect to user page
     */
    @PostMapping("user/edit/updatePassword")
    public String tryChangePassword(@ModelAttribute("updatePasswordDTO") UpdatePasswordDTO updatePasswordDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> logger.info(error.getDefaultMessage()));
        }
        try {
            updatePasswordService.updatePassword(updatePasswordDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully.");
            return "redirect:/user";
        } catch (IllegalArgumentException e) {
            logger.warn("Form submission error: {}", e.getMessage());

            List<String> errorsList = List.of(e.getMessage().split("(?<=\\.) "));

            redirectAttributes.addFlashAttribute("errorMessages", errorsList);
            redirectAttributes.addFlashAttribute("updatePasswordDTO", updatePasswordDTO);

            return "redirect:/user/edit/updatePassword";
        }
    }
}

package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.service.UpdatePasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Controller
public class ChangePasswordController {
    Logger logger = LoggerFactory.getLogger(RegisterController.class);
    private UpdatePasswordService updatePasswordService;
    @Autowired
    public ChangePasswordController(UpdatePasswordService updatePasswordService) {
        this.updatePasswordService = updatePasswordService;
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

    @PostMapping("/change-password")
    public String tryChangePassword(@ModelAttribute("userDTO") UpdatePasswordDTO updatePasswordDTO, Model model) {

        updatePasswordService.validatePassword(updatePasswordDTO);

        return "mainTemplate";
    }

}

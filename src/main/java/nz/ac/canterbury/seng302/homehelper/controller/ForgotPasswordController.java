package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.event.OnResetPasswordSubmittedEvent;
import nz.ac.canterbury.seng302.homehelper.service.ForgotPasswordService;
import nz.ac.canterbury.seng302.homehelper.service.VerificationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ForgotPasswordController {

    Logger logger = LoggerFactory.getLogger(HomeController.class);
    ForgotPasswordService forgotPasswordService;
    VerificationCodeService verificationCodeService;

    @Autowired
    public ForgotPasswordController(ForgotPasswordService forgotPasswordService, VerificationCodeService verificationCodeService) {
        this.forgotPasswordService = forgotPasswordService;
        this.verificationCodeService = verificationCodeService;
    }

    @GetMapping("/password/forgot")
    public String forgotPassword() {
        logger.info("GET /password/forgot");
        return "forgotPasswordTemplate";
    }

    @PostMapping("/password/forgot")
    public String submitEmail(@RequestParam("email") String email, Model model, HttpServletRequest request) {
        logger.info("POST /password/forgot");
        String errorMessage = forgotPasswordService.validateEmail(email, request.getLocale());
        if (errorMessage.isEmpty()) {
            model.addAttribute("emailMessage", "An email was sent to the address if it was recognised");

        } else {
            model.addAttribute("errorMessage", errorMessage);
        }
        return "forgotPasswordTemplate";
    }

    @GetMapping("/password/reset/{token}")
    public String resetPassword(@PathVariable String token, RedirectAttributes redirectAttributes, Model model) {
        logger.info("GET /password/forgot/{}", token);
        Optional<User> expectedUser = verificationCodeService.getUserByToken(token);
        if (expectedUser.isPresent()) {
            model.addAttribute("token", token);
            return "resetPasswordTemplate";
        }
        redirectAttributes.addAttribute("error", "Reset password link has expired");
        return "redirect:/login";
    }

    @PostMapping("/password/reset/{token}")
    public String submitEmail(@PathVariable String token, @RequestParam("newPassword") String newPassword,
                              @RequestParam("retypePassword") String retypePassword,
                              RedirectAttributes redirectAttributes,
                              Model model,
                              HttpServletRequest request) {
        logger.info("POST /password/forgot/{}", token);
        Optional<User> expectedUser = verificationCodeService.getUserByToken(token);
        if (expectedUser.isPresent()) {
            User user = expectedUser.get();
            List<String> errors = forgotPasswordService.validatePasswords(newPassword, retypePassword);
            if (!errors.isEmpty()) {
                model.addAttribute("errorMessages", errors);
                model.addAttribute("token", token);
                return "resetPasswordTemplate";
            }
            verificationCodeService.consumeResetPasswordToken(token);
            request.getContextPath();
            forgotPasswordService.sendNewPasswordEmail(user.getEmail(), user.getFirstName(), request.getLocale());
            forgotPasswordService.updatePassword(user, newPassword);
            return "redirect:/login";
        }
        redirectAttributes.addAttribute("error", "Reset password link has expired");
        return "redirect:/login";
    }
}

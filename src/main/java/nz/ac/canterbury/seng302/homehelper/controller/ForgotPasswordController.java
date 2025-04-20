package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.User;
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

/**
 * Controller for forgot password and reset password pages.
 */
@Controller
public class ForgotPasswordController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final ForgotPasswordService forgotPasswordService;

    private final VerificationCodeService verificationCodeService;

    /**
     * ForgotPasswordController constructor
     * @param forgotPasswordService Service for ForgotPasswordController
     * @param verificationCodeService Service to create and delete codes
     */
    @Autowired
    public ForgotPasswordController(ForgotPasswordService forgotPasswordService, VerificationCodeService verificationCodeService) {
        this.forgotPasswordService = forgotPasswordService;
        this.verificationCodeService = verificationCodeService;
    }

    /**
     * Get the forgot password form.
     * @return Template for forgot password page
     */
    @GetMapping("/password/forgot")
    public String forgotPassword() {
        logger.info("GET /password/forgot");
        return "forgotPasswordTemplate";
    }

    /**
     * Post request method for forgot password page. Checks whether email is in a valid format and sets error message if there
     * are any problems. Otherwise, sets an email confirmation message, and if the email is associated to an account, a token
     * will be created and an email will be sent to that account in order to reset their password.
     * @param email Email associated with the user to have their password reset.
     * @param request Post request used to get locale
     * @return Template for forgot password page
     */
    @PostMapping("/password/forgot")
    public String submitEmail(@RequestParam("email") String email, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        logger.info("POST /password/forgot");
        String errorMessage = forgotPasswordService.validateEmail(email, request.getLocale());

        if (errorMessage.isEmpty()) {
            redirectAttributes.addFlashAttribute("emailMessage",
                    "An email was sent to the address if it was recognised");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
        }

        return "redirect:/password/forgot";
    }

    /**
     * Get the forgot password form if the token is valid and associated to an account. Otherwise, it redirects the user to
     * the login page with message.
     * @param token Reset password token for user
     * @param redirectAttributes Message for redirect page
     * @param model Model interface
     * @return Template for reset password page or redirect to login page
     */
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

    /**
     * Post request method for reset password page. If the token is valid and associated to an account, it checks if the new
     * and retyped passwords are in the right format and updates user details if true. If the passwords aren't in the right format,
     * an error message shows up. If the token is invalid, it redirects the user to the login page with message.
     * @param token Reset password token for user
     * @param newPassword New password for user
     * @param retypePassword Retype new password for confirmation
     * @param redirectAttributes Message for redirect page
     * @param request Post request used to get locale
     * @return Template for reset password page or redirect to login page
     */
    @PostMapping("/password/reset/{token}")
    public String submitPassword(@PathVariable String token, @RequestParam("newPassword") String newPassword,
                              @RequestParam("retypePassword") String retypePassword,
                              RedirectAttributes redirectAttributes,
                              HttpServletRequest request) {
        logger.info("POST /password/forgot/{}", token);
        Optional<User> expectedUser = verificationCodeService.getUserByToken(token);
        if (expectedUser.isPresent()) {
            User user = expectedUser.get();
            List<String> errors = forgotPasswordService.validatePasswords(newPassword, retypePassword);
            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessages", errors);
                redirectAttributes.addFlashAttribute("token", token);
                return "redirect:/password/reset/" + token;
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

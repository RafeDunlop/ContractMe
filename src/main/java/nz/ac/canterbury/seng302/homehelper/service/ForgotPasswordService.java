package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.event.OnResetPasswordSubmittedEvent;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.thymeleaf.context.Context;
import java.util.*;

/**
 * Service for forgot password and reset password pages.
 */
@Service
public class ForgotPasswordService {

    private final UserRepository userRepository;

    private final UserValidation userValidation;

    private final ApplicationEventPublisher eventPublisher;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    /**
     * ForgotPasswordService constructor
     * @param userRepository Repository for user entities
     * @param userValidation Validation for user details
     * @param eventPublisher Set up emails and token expiry
     * @param emailService Send emails to users
     */
    @Autowired
    public ForgotPasswordService (UserRepository userRepository, UserValidation userValidation, ApplicationEventPublisher eventPublisher, EmailService emailService) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Validate the given email string. Check whether user with the email exists and if it's in the correct format.
     * Sets an event to generate token and send email if true.
     * @param email Inputted email
     * @param locale Region/Language preference
     * @return Error message about email
     */
    public String validateEmail(String email, Locale locale) {
        Optional<User> expectedUser = userRepository.findByEmailIgnoreCase(email);

        expectedUser.ifPresent(user -> eventPublisher.publishEvent(new OnResetPasswordSubmittedEvent(user, locale)));
        return String.join("", userValidation.validateEmailString(email));
    }

    /**
     * Email the user to let them know that their password has been reset.
     * @param email Email address to send to
     * @param firstName First name of user for email heading
     * @param locale Region/Language preference
     */
    public void sendNewPasswordEmail(String email, String firstName, Locale locale) {
        Context context = new Context(locale);
        context.setVariable("name", firstName);
        emailService.createEmail(
                email,
                "Password Updated",
                "html/email-confirm-new-password",
                context,
                "Send confirmation of password change email failed"
        );
    }

    /**
     * Validate inputted passwords by sending details to UserValidation and return list of errors.
     * @param newPassword New password for user
     * @param confirmPassword Confirm new password
     * @return Mapping of password errors
     */
    public Map<String, List<String>> validatePasswords(String newPassword, String confirmPassword) {
        Map<String, List<String>> errors = new HashMap<>();

        putIfNotEmpty(errors, "newPasswordError", userValidation.validatePasswordString(newPassword));
        putIfNotEmpty(errors, "confirmNewPasswordError", userValidation.validateConfirmPasswordString(
                newPassword, confirmPassword, "resetPassword"));

        return errors;
    }

    /**
     * Inserts a key-value pair into the provided map if the list of messages is not null or empty.
     * @param map       the map to insert the key-value pair into
     * @param key       the key to associate with the messages
     * @param messages  the list of error messages to insert if not empty
     */
    private void putIfNotEmpty(Map<String, List<String>> map, String key, List<String> messages) {
        if (messages != null && !messages.isEmpty()) {
            map.put(key, messages);
        }
    }

    /**
     * Reset the user's password by encoding it before updating the user in the repository.
     * @param user User with reset password
     * @param newPassword New password for user
     */
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }
}

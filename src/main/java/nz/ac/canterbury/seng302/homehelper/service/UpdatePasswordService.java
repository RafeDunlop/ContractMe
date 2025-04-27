package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Handles calling the validation for updating the password
 * Checks database to compare current password
 * Checks that the new and retyped passwords are the same
 */
@Service
public class UpdatePasswordService {
    private final UserValidation userValidation;
    private final LoginService loginService;
    private final EmailService emailService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private Logger logger;

    /**
     * Constructor for the UpdatePasswordService class
     */
    @Autowired
    public UpdatePasswordService(UserValidation userValidation, LoginService loginService,UserRepository userRepository, EmailService emailService) {
        this.userValidation = userValidation;
        this.loginService = loginService;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Updates the user's password in the database after successful validation.
     * Also sends a confirmation email.
     *
     * @param updatePasswordDTO Data transfer object containing the current password, new password, and retyped password.
     */
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        User user = loginService.getUserByEmail();
        String password = updatePasswordDTO.getNewPassword();

        // Updates the Users password to the new Password.
        user.setPassword(passwordEncoder.encode(password));

        //Save Users New Password
        userRepository.save(user);

        try {
            emailService.sendUpdatePasswordConfirmation(user.getEmail(), user.getFirstName(), java.util.Locale.getDefault());
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
    }

    /**
     * Validates the user's password update request.
     * Checks that the current password is correct, that the new password meets validation rules,
     * and that the new password matches the retyped password.
     *
     * @param updatePasswordDTO Data transfer object containing the current password, new password, and retyped password.
     * @return a map containing lists of error messages, grouped by field.
     */
    public Map<String, List<String>> updatePasswordValidation(UpdatePasswordDTO updatePasswordDTO) {
        Map<String, List<String>> errors = new HashMap<>();
        User user = loginService.getUserByEmail();
        String password = updatePasswordDTO.getNewPassword();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();

        List<String> currentPasswordErrors = new ArrayList<>();
        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), user.getPassword())) {
            currentPasswordErrors.add("Your old password is incorrect.");
        }
        putIfNotEmpty(errors, "oldPasswordError", currentPasswordErrors);

        putIfNotEmpty(errors, "newPasswordError", userValidation.validateUpdatePasswordString(password, firstName, lastName, email));
        putIfNotEmpty(errors, "newPasswordError", userValidation.validatePasswordString(updatePasswordDTO.getNewPassword()));
        putIfNotEmpty(errors, "newPasswordRetypeError", userValidation.validateConfirmPasswordString(updatePasswordDTO.getNewPassword(), updatePasswordDTO.getRetypePassword(), "updatePassword"));

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
}

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
import java.util.List;
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
    public UpdatePasswordService(UserValidation userValidation, LoginService loginService,
            UserRepository userRepository, EmailService emailService) {
        this.userValidation = userValidation;
        this.loginService = loginService;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Handles validation the DTO to check the retyped passwords match and the
     * current password is correct
     * Saves the users password in the database if there are no errors
     * Throws an IllegalArgumentException containing a list of errors to display on
     * the webpage.
     *
     * @param updatePasswordDTO Data transfer object for updating the password
     */
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        List<String> errors = new ArrayList<>();
        User user = loginService.getUserByEmail();
        String password = updatePasswordDTO.getNewPassword();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String email = user.getEmail();
        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), user.getPassword())) {
            errors.add("Your old password is incorrect.");
        }

        // Checks second two fields are the same and that the passwords match the
        // patterns
        errors.addAll(userValidation.validatePasswordString(
                updatePasswordDTO.getNewPassword(), updatePasswordDTO.getRetypePassword(),
                firstName, lastName, email, "updatePassword"));
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        // Updates the Users password to the new Password.
        user.setPassword(passwordEncoder.encode(password));

        // Save Users New Password
        userRepository.save(user);

        try {
            emailService.sendUpdatePasswordConfirmation(user.getEmail(), user.getFirstName(),
                    java.util.Locale.getDefault());
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
    }
}

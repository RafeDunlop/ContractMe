package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RegisterService {

    private static final Logger log = LoggerFactory.getLogger(RegisterService.class);
    private final UserRepository userRepository;
    private final UserValidation userValidation;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegisterService(UserRepository userRepository, UserValidation userValidation,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Create a user and save it to the database, with validation, first name, last name, email and password must not be null or empty.
     * @param userRegisterDTO Data transfer object for user registration
     * @return the user if it was saved successfully
     * @throws IllegalArgumentException if the firstName, lastName, email or password inputs are invalid
     */
    public User registerUser(UserRegisterDTO userRegisterDTO) throws IllegalArgumentException {
        // Throw error if data is not received into the service class correctly
        if (userRegisterDTO == null) {
            throw new IllegalArgumentException("Data integration error");
        }

        String firstName = userRegisterDTO.getFirstName();
        String lastName = userRegisterDTO.getLastName();
        String email = userRegisterDTO.getEmail();
        String password = userRegisterDTO.getPassword();
        String confirmPassword = userRegisterDTO.getConfirmPassword();

        List<String> errors = new ArrayList<>();

        errors.addAll(userValidation.validateNameString(firstName, "First"));
        errors.addAll(userValidation.validateNameString(lastName, "Last"));
        errors.addAll(validateEmail(email));
        errors.addAll(userValidation.validatePasswordString(password, confirmPassword,"registerPassword"));
        // Throw IllegalArgumentException if any errors occurred in validating the data
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        // Create a user entity
        User user = new User(firstName, lastName, email, passwordEncoder.encode(password));

        // Save entity to the user repository
        return userRepository.save(user);
    }

    /**
     * Validates if the email is already in use
     * @param email the email inputted by the user
     */
    public List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();

            Optional<User> existingUser = userRepository.findByEmailIgnoreCase(email);
            if (existingUser.isPresent()) {
                errors.add("This email address is already in use.");
            }

            errors.addAll(userValidation.validateEmailString(email));
        return errors;
    }
}

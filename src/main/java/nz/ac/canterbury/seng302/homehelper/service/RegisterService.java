package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service class responsible for handling user registration logic,
 * including validation of registration fields and password encoding.
 */
@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final UserValidation userValidation;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a {@code RegisterService} with the given dependencies.
     * @param userRepository  the repository used to access user data
     * @param userValidation  the utility used to validate user input fields
     */
    @Autowired
    public RegisterService(UserRepository userRepository, UserValidation userValidation) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Validates the registration fields provided in the {@code UserRegisterDTO}.
     * @param dto the user registration data transfer object containing user input fields
     * @return a map where the key is the field name and the value is a list of error messages
     *         associated with that field; if no errors exist for a field, it is not included
     */
    public Map<String, List<String>> validateRegistration(UserRegisterDTO dto) {
        Map<String, List<String>> errors = new HashMap<>();

        putIfNotEmpty(errors, "firstNameError", userValidation.validateNameString(dto.getFirstName(), "First"));
        putIfNotEmpty(errors, "lastNameError", userValidation.validateNameString(dto.getLastName(), "Last"));
        putIfNotEmpty(errors, "emailError", validateEmail(dto.getEmail()));
        putIfNotEmpty(errors, "passwordError", userValidation.validatePasswordString(dto.getPassword()));
        putIfNotEmpty(errors, "confirmPasswordError", userValidation.validateConfirmPasswordString(
                dto.getPassword(), dto.getConfirmPassword(), "registerPassword"));
        return errors;
    }

    /**
     * Create a user and save it to the database, with validation, first name, last name, email and password must not be null or empty.
     * @param userRegisterDTO Data transfer object for user registration
     * @return the user if it was saved successfully
     * @throws IllegalArgumentException if the firstName, lastName, email or password inputs are invalid
     */
    public User registerUser(UserRegisterDTO userRegisterDTO) {
        User user = new User(
                userRegisterDTO.getFirstName(),
                userRegisterDTO.getLastName(),
                userRegisterDTO.getEmail(),
                passwordEncoder.encode(userRegisterDTO.getPassword())
        );
        return userRepository.save(user);
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

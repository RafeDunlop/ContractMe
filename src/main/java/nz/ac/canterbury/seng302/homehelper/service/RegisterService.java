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

@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final UserValidation userValidation;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RegisterService(UserRepository userRepository, UserValidation userValidation) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    public Map<String, List<String>> validateRegistration(UserRegisterDTO dto) {
        Map<String, List<String>> errors = new HashMap<>();

        putIfNotEmpty(errors, "firstNameError", userValidation.validateNameString(dto.getFirstName(), "First"));
        putIfNotEmpty(errors, "lastNameError", userValidation.validateNameString(dto.getLastName(), "Last"));
        putIfNotEmpty(errors, "emailError", validateEmail(dto.getEmail()));

        List<String> passwordErrors = userValidation.validatePasswordString(
                dto.getPassword(), dto.getConfirmPassword(), "registerPassword"
        );

        // Separate out confirm password mismatch error
        List<String> confirmPasswordErrors = new ArrayList<>();
        passwordErrors.removeIf(err -> {
            if (err.equals("Passwords do not match.")) {
                confirmPasswordErrors.add(err);
                return true;
            }
            return false;
        });

        putIfNotEmpty(errors, "passwordError", passwordErrors);
        putIfNotEmpty(errors, "confirmPasswordError", confirmPasswordErrors);

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

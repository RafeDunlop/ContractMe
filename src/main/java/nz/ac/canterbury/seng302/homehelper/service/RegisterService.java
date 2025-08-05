package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.util.MapUtil;
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
    private final LocationService locationService;

    /**
     * Constructs a {@code RegisterService} with the given dependencies.
     *
     * @param userRepository the repository used to access user data
     * @param userValidation the utility used to validate user input fields
     */
    @Autowired
    public RegisterService(UserRepository userRepository, UserValidation userValidation, LocationService locationService) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        this.locationService = locationService;
    }

    /**
     * Validates the registration fields provided in the {@code UserRegisterDTO}.
     *
     * @param dto the user registration data transfer object containing user input
     *            fields
     * @return a map where the key is the field name and the value is a list of
     *         error messages
     *         associated with that field; if no errors exist for a field, it is not
     *         included
     */
    public Map<String, List<String>> validateRegistration(UserRegisterDTO dto) {
        Map<String, List<String>> errors = new HashMap<>();

        MapUtil.putIfNotEmpty(errors, "firstNameError", userValidation.validateNameString(dto.getFirstName(), "First"));
        MapUtil.putIfNotEmpty(errors, "lastNameError", userValidation.validateNameString(dto.getLastName(), "Last"));
        MapUtil.putIfNotEmpty(errors, "emailError", validateEmail(dto.getEmail()));
        MapUtil.putIfNotEmpty(errors, "passwordError", userValidation.validatePasswordString(
                dto.getPassword(), dto.getFirstName(), dto.getLastName(), dto.getEmail()));
        MapUtil.putIfNotEmpty(errors, "confirmPasswordError", userValidation.validateConfirmPasswordString(
                dto.getPassword(), dto.getConfirmPassword(), "registerPassword"));
        return errors;
    }

    /**
     * Create a user and save it to the database, with validation, first name, last
     * name, email and password must not be null or empty.
     *
     * @param userRegisterDTO Data transfer object for user registration
     * @return the user if it was saved successfully
     * @throws IllegalArgumentException if the firstName, lastName, email or
     *                                  password inputs are invalid
     */
    public User registerUser(UserRegisterDTO userRegisterDTO) {
        User user = new User(
                userRegisterDTO.getFirstName(),
                userRegisterDTO.getLastName(),
                userRegisterDTO.getEmail(),
                passwordEncoder.encode(userRegisterDTO.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Creates a location and attaches it to the user entity
     * Saves the user with its location to the database
     *
     * @param user       The user to attach location to
     * @param addressDTO Data transfer object for user registration
     */
    public void registerLocation(User user, AddressDTO addressDTO) {
        user.setLocation(locationService.locate(addressDTO));
        userRepository.save(user);
    }

    /**
     * Validates if the email is already in use
     *
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

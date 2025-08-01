package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final UserValidation userValidation;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LoginService(UserRepository userRepository, UserValidation userValidation) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Gets the user by their email, as stored in Spring Security;
     * allows for access of user info across multiple sessions
     * @return the User object if found
     * @throws IllegalArgumentException if the email address is invalid
     */
    public User getUserByEmail() throws IllegalArgumentException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        List<String> errors = userValidation.validateEmailString(userEmail);
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(userEmail);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        else {
            errors.add("The user email is invalid");
            throw new IllegalArgumentException(String.join(";", errors));
        }

    }

    /**
     * Get the user by their email and password.
     * @param email the user's email
     * @param password the user's password
     * @return the matching user
     * @throws NoSuchElementException if the user was not found
     * @throws IllegalArgumentException if the email is invalid
     */
    public User getUserByEmailAndPassword(String email, String password) throws NoSuchElementException, IllegalArgumentException {
        List<String> errors = userValidation.validateEmailString(email.trim());
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(email);
        if (optionalUser.isPresent() && passwordEncoder.matches(password, optionalUser.get().getPassword())) {
            return optionalUser.get();
        } else {
            errors.add("The email address is unknown, or the password is invalid.");
            throw new IllegalArgumentException(String.join(";", errors));
        }
    }
}

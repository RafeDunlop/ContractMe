package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for the edit profile page.
 */
@Service
public class EditProfileService {

    private final UserRepository userRepository;
    private final UserValidation userValidation;
    private final String UPLOAD_DIR = "profile_pictures/";

    /**
     * Constructor for the service and links the repository and validator to the
     * service.
     * @param userRepository UserRepository for getting and updating user details
     * @param userValidation UserValidation for validating updated user details
     */
    @Autowired
    public EditProfileService(UserRepository userRepository, UserValidation userValidation) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
    }

    /**
     * Sends updated user to validation method and service. Collects the returned
     * error messages and throws them to controller. If there are no errors, then the
     * user is updated with the new details in the database.
     * @param updatedUser User object with the updated user details
     * @param sameEmail Boolean on whether the email has been changed (true = same email,
     *                  false = different email)
     * @throws IllegalArgumentException Throws an error if there is are invalid updated details
     */
    public void updateUser(User updatedUser, boolean sameEmail) throws IllegalArgumentException {
        if (updatedUser == null) {
            throw new IllegalArgumentException("Data integration error");
        }

        // Validates updated user details and returns all errors
        List<String> errors = validateUpdate(updatedUser, sameEmail);

        // Throws all errors that were found
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        // Updates user details in database
        userRepository.save(updatedUser);
        if (!sameEmail) {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(updatedUser.getEmail(), null, updatedUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }

    /**
     * Validates the first/last name and email updates on the edit profile page.
     * Checks if the fields are in the correct format and then if the email doesn't
     * already exist in the database. Adds all errors to a list which is returned.
     * @param updatedUser User object with the updated user details
     * @param sameEmail Boolean on whether the email has been changed (true = same email,
     *                  false = different email)
     * @return A list of error message strings
     */
    private List<String> validateUpdate(User updatedUser, boolean sameEmail) {
        String firstName = updatedUser.getFirstName();
        String lastName = updatedUser.getLastName();
        String email = updatedUser.getEmail();
        List<String> errors = new ArrayList<>();

        // Validates user first/last name
        errors.addAll(userValidation.validateNameString(firstName, "First"));
        errors.addAll(userValidation.validateNameString(lastName, "Last"));

        // Validates user email format and if valid, checks if email doesn't exist
        List<String> emailFormatError = userValidation.validateEmailString(email);
        errors.addAll(emailFormatError);
        if (emailFormatError.isEmpty()) {
            if (!sameEmail && userRepository.findByEmailIgnoreCase(email).isPresent()) {
                errors.add("This email address is already in use.");
            }
        }

        return errors;
    }

    /**
     * Updates the user Profile Picture
     * The raw data of the profile picture is stored locally in the profile_picture directory
     * path the path of the newly uploaded profile picture is stored under the users profilePicture in the repository
     *
     * @param user The user that changing their profile picture
     * @param profilePicture raw data of profile picture
     */
    public void updateProfilePicture(User user, MultipartFile profilePicture) {
        try {
            // Generate unique filename
            String fileName = UUID.randomUUID() + "_" + profilePicture.getOriginalFilename().replaceAll("[^a-zA-Z0-9.]", "_");
            Path uploadPath = Paths.get(UPLOAD_DIR);

            // Ensure the directory exists
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file to 'profile_pictures'
            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, profilePicture.getBytes(), StandardOpenOption.CREATE_NEW);

            // Store only the relative path in the database
            user.setProfilePicture(fileName);
            userRepository.save(user);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}

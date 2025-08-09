package nz.ac.canterbury.seng302.homehelper.service;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.imageio.ImageIO;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;

/**
 * Service for the edit profile page.
 */
@Service
public class EditProfileService {

    private final UserRepository userRepository;
    private final UserValidation userValidation;
    private final LocationService locationService;
    private final String UPLOAD_DIR = "profile_pictures/";

    /**
     * Constructor for the service and links the repository and validator to the
     * service.
     * @param userRepository UserRepository for getting and updating user details
     * @param userValidation UserValidation for validating updated user details
     */
    @Autowired
    public EditProfileService(UserRepository userRepository, UserValidation userValidation, LocationService locationService) {
        this.userRepository = userRepository;
        this.userValidation = userValidation;
        this.locationService = locationService;
    }

    /**
     * Sends updated user to validation method and service. Collects the returned
     * error messages and throws them to controller. If there are no errors, then the
     * user is updated with the new details in the database.
     * @param updatedUser User object with the updated user details
     */
    public void updateUser(User updatedUser)  {
        if (updatedUser == null) {
            throw new IllegalArgumentException("Data integration error");
        }

        userRepository.save(updatedUser);

        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUser.getEmail(),
                currentAuth.getCredentials(),
                currentAuth.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    public User updateUserLocation(User currentUser, AddressDTO addressDTO) {
        Location currentLocation = currentUser.getLocation();
        addressDTO = locationService.updateEditedLocation(currentLocation, addressDTO);
        currentUser.setLocation(locationService.locate(addressDTO));
        return currentUser;
    }

    public Contractor updateContractor(UserRegisterDTO userRegisterDTO, Contractor contractor) {
        contractor.setCountryCode(userRegisterDTO.getCountryCode());
        contractor.setPhoneNumber(userRegisterDTO.getPhoneNumber());
        contractor.setHourlyRate(userRegisterDTO.getHourlyRate());
        return contractor;
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
    public Map<String, List<String>> validateUpdate(User updatedUser, boolean sameEmail) {
        String firstName = updatedUser.getFirstName();
        String lastName = updatedUser.getLastName();
        String email = updatedUser.getEmail();

        Map<String, List<String>> errors = new HashMap<>();

        // Validate first name
        List<String> firstNameErrors = userValidation.validateNameString(firstName, "First");
        if (!firstNameErrors.isEmpty()) {
            errors.put("firstNameError", firstNameErrors);
        }

        // Validate last name
        List<String> lastNameErrors = userValidation.validateNameString(lastName, "Last");
        if (!lastNameErrors.isEmpty()) {
            errors.put("lastNameError", lastNameErrors);
        }

        // Validate email format
        List<String> emailFormatErrors = userValidation.validateEmailString(email);
        if (!emailFormatErrors.isEmpty()) {
            errors.put("emailError", emailFormatErrors);
        } else {
            // If format is fine, check uniqueness
            if (!sameEmail && userRepository.findByEmailIgnoreCase(email).isPresent()) {
                errors.computeIfAbsent("emailError", k -> new ArrayList<>())
                        .add("This email address is already in use.");
            }
        }

        return errors;
    }


    /**
     * Updates the user Profile Picture
     * The profile picture file is stored in the profile_picture directory
     * The profile picture file name is stored in the users repository
     *
     * @param user The user that changing their profile picture
     * @param profilePicture profile picture file
     */
    public List<String> updateProfilePicture(User user, MultipartFile profilePicture) {
        List<String> errors = new ArrayList<>();

        if (profilePicture.isEmpty()) {
            errors.add("No file selected.");
            return errors;
        }

        errors.addAll(userValidation.validateProfilePicture(profilePicture));
        if (!errors.isEmpty()) {
            return errors;
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a unique filename
            String filename = UUID.randomUUID() + "_" + profilePicture.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);

            BufferedImage originalImage = ImageIO.read(profilePicture.getInputStream());

            // Resize the image to 200x200
            int minSize = Math.min(originalImage.getWidth(), originalImage.getHeight());
            int x = (originalImage.getWidth() - minSize) / 2;
            int y = (originalImage.getHeight() - minSize) / 2;
            BufferedImage croppedImage = originalImage.getSubimage(x, y, minSize, minSize);
            BufferedImage resizedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(croppedImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();

            File outputFile = filePath.toFile();
            ImageIO.write(resizedImage, "jpg", outputFile);

            user.setProfilePicture(filename);
            userRepository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.");
        }

        return errors;
    }
}

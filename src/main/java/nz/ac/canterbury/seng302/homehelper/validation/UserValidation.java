package nz.ac.canterbury.seng302.homehelper.validation;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserValidation {

    /**
     * Validates whether the email is in the correct form.
     *
     * @param email The email string inputted by the user
     * @return A list of errors that the inputted email generated
     */
    public List<String> validateEmailString(String email) {
        List<String> errors = new ArrayList<>();

        // Check if email is empty, null, or not in the form 'jane@doe.nz'
        if (email == null || email.trim().isEmpty() ||
                !email.matches("^[A-Za-z0-9]+([+_.-][A-Za-z0-9]+)*@[A-Za-z0-9]+([.-][A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")) {
            errors.add("Email address must be in the form 'jane@doe.nz'.");
        }
        return errors;
    }

    /**
     * Validates the first and last names
     *
     * @param name     the inputted name
     * @param nameType the type of name inputted, either first name or last name
     * @return A list of errors that the inputted name generated
     */
    public List<String> validateNameString(String name, String nameType) {
        List<String> errors = new ArrayList<>();

        if (name == null) {
            errors.add(nameType + " name cannot be empty.");
            return errors;
        }
        // Check name is not empty
        if (Objects.equals(nameType, "First") && (name.trim().isEmpty())) {
            errors.add(nameType + " name cannot be empty.");
        }

        // check name only includes letters, spaces, hyphens, or apostrophes
        if (!name.matches("^[\\p{L}\\-\\s']*$")) {
            errors.add(nameType + " name must only include letters, spaces, hyphens, or apostrophes.");
        }

        // Check name is less than or equal to 64 characters long
        if (name.length() > 64) {
            errors.add(nameType + " name must be 64 characters long or less.");
        }

        return errors;
    }

    /**
     * Validates password strength
     *
     * @param password the inputted password
     * @return A list of errors that the inputted password generated
     */
    // Note this function is for validating passwords for registration, not logging in
    public List<String> validatePasswordString(String password, String firstName, String lastName, String email) {
        List<String> errors = new ArrayList<>();

        // Check password is at least 8 characters long, includes an uppercase letter, a lowercase letter, a number and a special character
        if (passwordContainsFields(password, firstName, lastName, email) ||
                password.length() < 8 ||
                !password.matches(".*[A-Z].*") ||  // At least one uppercase
                !password.matches(".*[a-z].*") ||  // At least one lowercase
                !password.matches(".*\\d.*") ||    // At least one number
                !password.matches(".*[^a-zA-Z0-9].*")) { // At least one special char
            errors.add(
                    "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email).");
        }

        return errors;
    }

    /**
     * Checks if the new password contains the user's name or email.
     *
     * @param password  the plaintext password to validate
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @param email     the user's email
     * @return true if the password contains any of the given fields,
     *         false otherwise.
     */
    public boolean passwordContainsFields(String password, String firstName, String lastName, String email) {
        Pattern pattern = Pattern.compile(
                "(" + Pattern.quote(firstName) + ")"
                + (!(lastName == null || lastName.isBlank()) ? "|(" + Pattern.quote(lastName) + ")" : "")
                + "|("
                + Pattern.quote(email) + ")",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(password);
        return password != null && matcher.find();
    }

     /** Validates password matches confirm password
     * @param password the inputted password
     * @return A list of errors that the inputted password generated
     * @param confirmPassword the retyped password
     * @param type the method the validation was called from
     */
    public List<String> validateConfirmPasswordString(String password, String confirmPassword, String type) {
        List<String> errors = new ArrayList<>();

        // Compares password and confirm password
        if (!password.equals(confirmPassword)) {
            switch (type) {
                case "updatePassword" -> errors.add("The new passwords do not match.");
                case "registerPassword" -> errors.add("Passwords do not match.");
                case "resetPassword" -> errors.add("The passwords do not match.");
            }
        }
        return errors;
    }

    /**
     * checks if the provided file is of an allowed image type (PNG, JPG)
     * and ensures that its size does not exceed the limit (10MB).
     *
     * @param profilePicture Uploaded profile picture raw data file
     * @return A list of errors that the inputted profile picture generated
     */
    public List<String> validateProfilePicture(MultipartFile profilePicture) {
        List<String> errors = new ArrayList<>();

        // Check if the file is empty
        if (profilePicture.isEmpty()) {
            errors.add("No file selected.");
            return errors;
        }

        // Allowed MIME types
        List<String> allowedMimeTypes = List.of("image/jpeg", "image/png");

        // Check file type
        if (!allowedMimeTypes.contains(profilePicture.getContentType())) {
            errors.add("Image must be of type png, jpg or svg.");
        }

        // Check file size
        long maxSizeBytes = 10 * 1024 * 1024; // 10MB
        if (profilePicture.getSize() > maxSizeBytes) {
            errors.add("Image must be less than 10MB.");
        }

        return errors;
    }
}

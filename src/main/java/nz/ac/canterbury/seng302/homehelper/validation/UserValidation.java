package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserValidation {

    /**
     * Validates whether the email is in the correct form.
     * @param email The email string inputted by the user
     * @return A list of errors that the inputted email generated
     */
    public List<String> validateEmailString(String email) {
        List<String> errors = new ArrayList<>();

        // Check if email is empty, null, or not in the form 'jane@doe.nz'
        if (email == null || email.trim().isEmpty() ||
                !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$")) {
            errors.add("Email address must be in the form ‘jane@doe.nz’.");
        }
        return errors;
    }

    /**
     * Validates the first and last names
     * @param name     the inputted name
     * @param nameType the type of name inputted, either first name or last name
     */
    public List<String> validateNameString(String name, String nameType) {
        List<String> errors = new ArrayList<>();

        // Check name is not empty
        if (Objects.equals(nameType, "First") && (name == null || name.trim().isEmpty())) {
            errors.add(nameType + " name cannot be empty.");
        }

        // check name only includes letters, spaces, hyphens, or apostrophes
        else if (!name.matches("^[\\p{L}\\-\\s']*$")) {
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
     * @param password the inputted password
     * @param confirmPassword the retyped password
     * @param type the method the validation was called from
     */
    // Note this function is for validating passwords for registration, not logging in
    public List<String> validatePasswordString(String password, String confirmPassword, String type) {
        List<String> errors = new ArrayList<>();

        // Compares password and confirm password
        if (!password.equals(confirmPassword)) {
            if (type.equals("updatePassword")) {
                errors.add("New Passwords do not match.");
            }
            else if (type.equals("registerPassword")) {
                errors.add("Passwords do not match.");
            }
        }

        // Check password is at least 8 characters long, includes an uppercase letter, a lowercase letter, a number and a special character
        if (password == null || password.length() < 8 ||
                !password.matches(".*[A-Z].*") ||  // At least one uppercase
                !password.matches(".*[a-z].*") ||  // At least one lowercase
                !password.matches(".*\\d.*") ||    // At least one number
                !password.matches(".*[^a-zA-Z0-9].*")) { // At least one special char
            errors.add("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.");
        }

        return errors;
    }
    // Note this function checks if the new password contains the user's name or email.
    public List<String> validateUpdatePasswordString(String password, String confirmPassword, String type,
            String firstName,String lastName,String email) {
        List<String> errors = new ArrayList<>();

        if (password != null && (password.contains( firstName) || (password.contains( lastName) || (password.contains( email))))){
            errors.add("Your password should not contain your name or email address.");
        }
        return errors;
    }

}

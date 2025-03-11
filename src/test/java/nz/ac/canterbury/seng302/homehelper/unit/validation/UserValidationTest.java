package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class UserValidationTest {

    /**
     * Test to see if the regex in the validateEmailString method accepts valid email addresses. All emails contain a
     * valid username, one '@', and a domain with a gTLD (generic top-level domain) or a ccTLD (country code top-level
     * domain), all in that order.
     */
    @Test
    public void PasswordValidation_ValidEmails_AcceptInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> validEmails = List.of("jane@doe.com", "abc@nz.nz.nz.nz", "abc-123.456@gmail1234.com");
        for (String email : validEmails) {
            Assertions.assertTrue(userValidation.validateEmailString(email).isEmpty());
        }
    }

    /**
     * Test to see if the regex in the validateEmailString method rejects invalid email addresses. The invalid emails
     * either contain no username, more than one '@', multiple periods un a row, or an invalid gTLD or ccTLD.
     */
    @Test
    public void PasswordValidation_InvalidEmails_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of("Email address must be in the form ‘jane@doe.nz’.");
        List<String> invalidEmails = Arrays.asList("@doe.com", "abc@@nz.nz.nz.nz", "abc@gmail..om", "jane@doe.n");
        for (String email : invalidEmails) {
            Assertions.assertLinesMatch(expectedErrorList, userValidation.validateEmailString(email));
        }
    }

    /**
     * Test to see if the regex in the validateNameString method accepts valid names. Allows any character from
     * any language along with hyphens, apostrophes, and spaces.
     */
    @Test
    public void NameValidation_ValidNames_AcceptInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> validNames = List.of("Māori", "Müller-Müller", "Jane Doe", "O'Conner");
        for (String name : validNames) {
            Assertions.assertTrue(userValidation.validateNameString(name, "First").isEmpty());
        }
    }

    /**
     * Test to see if the regex in the validateNameString method rejects invalid names. Any characters that aren't letters,
     * hyphens, apostrophes, or spaces aren't accepted.
     */
    @Test
    public void NameValidation_MissingNames_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedRegexErrorList = List.of( "First name must only include letters, spaces, hyphens, or apostrophes.");
        List<String> validNames = List.of("Müller!", "Jane-Doe1");
        for (String name : validNames) {
            Assertions.assertLinesMatch(expectedRegexErrorList, userValidation.validateNameString(name, "First"));
        }
    }

    /**
     * Test to see if the regex in the validateNameString method rejects missing first names. Missing last names should be accepted.
     */
    @Test
    public void NameValidation_InvalidNames_RejectFirstNameInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedRegexErrorList = List.of( "First name cannot be empty.");
        List<String> validNames = List.of("", "  ");
        for (String name : validNames) {
            Assertions.assertLinesMatch(expectedRegexErrorList, userValidation.validateNameString(name, "First"));
        }
        for (String name : validNames) {
            Assertions.assertLinesMatch(List.of(), userValidation.validateNameString(name, "Last"));
        }
    }

    /**
     * Test to see if correct error message is displayed when password doesn't much confirmPassword
     * Expects: 'Passwords do not match.' error
     */
    @Test
    public void PasswordValidation_PasswordDoesNotMatch_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of("Passwords do not match.");
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Test123!", "Password123!"));
    }

    /**
     * Test to see if the correct error message is displayed when there is no uppercase letter in password
     * Expects: 'Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.' error
     */
    @Test
    public void PasswordValidation_PasswordsMatchNoUppercaseLetter_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("password123!", "password123!"));
    }

    /**
     * Test to see if the correct error message is displayed when there is no lowercase letter in password
     * Expects: 'Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.' error
     */
    @Test
    public void PasswordValidation_PasswordsMatchNoLowercaseLetter_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("PASSWORD123!", "PASSWORD123!"));
    }

    /**
     * Test to see if the correct error message is displayed when there is no number in password
     * Expects: 'Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.' error
     */
    @Test
    public void PasswordValidation_PasswordsMatchNoNumber_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Password!", "Password!"));
    }

    /**
     * Test to see if the correct error message is displayed when there is no special character in password
     * Expects: 'Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.' error
     */
    @Test
    public void PasswordValidation_PasswordsMatchNoSpecialCharacter_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Password123", "Password123"));
    }

    /**
     * Test to see if the correct error message is displayed when there is less than 8 characters in password
     * Expects: 'Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.' error
     */
    @Test
    public void PasswordValidation_PasswordsMatchLessThanEightCharacters_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Test1!", "Test1!"));
    }

    /**
     * Test to see if no error message is displayed when a valid password that matches the confirmPassword is entered
     * Expects: empty List
     */
    @Test
    public void PasswordValidation_ValidPasswordThatMatch_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        Assertions.assertTrue(userValidation.validatePasswordString("Test123!", "Test123!").isEmpty());
    }
}

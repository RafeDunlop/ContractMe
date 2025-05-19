package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class UserValidationTest {

    @Test
    public void PasswordValidation_ValidEmails_AcceptInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> validEmails = List.of("jane@doe.com", "abc@nz.nz.nz.nz", "abc-123.456@gmail1234.com");
        for (String email : validEmails) {
            Assertions.assertTrue(userValidation.validateEmailString(email).isEmpty());
        }
    }

    @Test
    public void PasswordValidation_InvalidEmails_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of("Email address must be in the form 'jane@doe.nz'.");
        List<String> invalidEmails = Arrays.asList("@doe.com", "abc@@nz.nz.nz.nz", "abc@gmail..om", "jane@doe.n");
        for (String email : invalidEmails) {
            Assertions.assertLinesMatch(expectedErrorList, userValidation.validateEmailString(email));
        }
    }

    @Test
    public void NameValidation_ValidNames_AcceptInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> validNames = List.of("Māori", "Müller-Müller", "Jane Doe", "O'Conner");
        for (String name : validNames) {
            Assertions.assertTrue(userValidation.validateNameString(name, "First").isEmpty());
        }
    }

    @Test
    public void NameValidation_MissingNames_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedRegexErrorList = List.of( "First name must only include letters, spaces, hyphens, or apostrophes.");
        List<String> validNames = List.of("Müller!", "Jane-Doe1");
        for (String name : validNames) {
            Assertions.assertLinesMatch(expectedRegexErrorList, userValidation.validateNameString(name, "First"));
        }
    }

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

    @Test
    public void PasswordValidation_PasswordDoesNotMatch_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of("Passwords do not match.");
        Assertions.assertEquals(expectedErrorList, userValidation.validateConfirmPasswordString("Test123!", "Password123!","registerPassword"));
    }

    @Test
    public void PasswordValidation_NoUppercaseLetter_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("password123!", "john", "smith", "john.smith@example.com"));
    }

    @Test
    public void PasswordValidation_NoLowercaseLetter_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("PASSWORD123!", "john", "smith", "john.smith@example.com"));
    }

    @Test
    public void PasswordValidation_PasswordsMatchNoNumber_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Password!", "John", "Smith", "john.smith@example.com"));
    }

    @Test
    public void PasswordValidation_PasswordsMatchNoSpecialCharacter_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Password123", "John", "Smith", "john.smith@example.com"));
    }

    @Test
    public void PasswordValidation_PasswordsMatchLessThanEightCharacters_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Test1!", "John", "Smith", "john.smith@example.com"));
    }


    @Test
    public void PasswordValidation_ContainsEmail_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Abhisekh123!#", "Abhisekh","Chand","Abhisekh23@gmail.com"));
    }

    @Test
    public void PasswordValidation_ContainsFirstName_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Abhisekh123!#", "Abhisekh","Chand","Donald23@gmail.com"));
    }

    @Test
    public void PasswordValidation_ContainsLastName_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."
        );
        Assertions.assertEquals(expectedErrorList, userValidation.validatePasswordString("Chand123!#", "Abhisekh","Chand","Donald23@gmail.com"));
    }

    @Test
    public void PasswordValidation_ContainsFirstNameDifferentCase_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> expectedErrorList = List.of(
                "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."
        );
        Assertions.assertEquals(
                expectedErrorList,
                userValidation.validatePasswordString("Passjohn123", "John", "Smith", "john.smith@example.com"));

    }

    @Test
    public void PasswordValidation_BlankLastName_AcceptInputs() {
        UserValidation userValidation = new UserValidation();
        List<String> result = userValidation.validatePasswordString("Test123!", "John", "", "john.smith@example.com");
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void PasswordValidation_ValidPasswordThatMatch_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        Assertions.assertTrue(userValidation.validateConfirmPasswordString("Test123!", "Test123!","registerPassword").isEmpty());
    }

    @Test
    public void ProfilePictureValidation_InvalidProfilePictureFileType_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        MultipartFile profilePicture = Mockito.mock(MultipartFile.class);
        when(profilePicture.getContentType()).thenReturn("application/pdf");
        when(profilePicture.getSize()).thenReturn(10000000L);
        List<String> expectedErrorList = List.of("Image must be of type png, jpg or svg.");

        Assertions.assertEquals(expectedErrorList, userValidation.validateProfilePicture(profilePicture));
    }

    @Test
    public void ProfilePictureValidation_InvalidProfilePictureFileSize_RejectInputs() {
        UserValidation userValidation = new UserValidation();
        MultipartFile profilePicture = Mockito.mock(MultipartFile.class);
        when(profilePicture.getContentType()).thenReturn("image/png");
        when(profilePicture.getSize()).thenReturn((10 * 1024 * 1024) + 1L);
        List<String> expectedErrorList = List.of("Image must be less than 10MB.");

        Assertions.assertEquals(expectedErrorList, userValidation.validateProfilePicture(profilePicture));
    }

    @Test
    public void ProfilePictureValidation_ValidProfilePicture_Success() {
        UserValidation userValidation = new UserValidation();
        MultipartFile profilePicture = Mockito.mock(MultipartFile.class);
        when(profilePicture.getContentType()).thenReturn("image/svg+xml");
        when(profilePicture.getSize()).thenReturn(0L);

        Assertions.assertTrue(userValidation.validateProfilePicture(profilePicture).isEmpty());
    }
}

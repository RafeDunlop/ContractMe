package nz.ac.canterbury.seng302.homehelper.integration.service;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.service.ForgotPasswordService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ForgotPasswordServiceIntegrationTest {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    private static Locale locale;

    private User resetUser;

    @BeforeAll
    static void passwordEncoderSetup() {
        locale = Locale.ENGLISH;
    }

    @BeforeEach
    void userSetup() {
        resetUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(resetUser);
    }

    @AfterEach
    void repositoryCleanup() {
        verificationCodeRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Tests validating the email used to reset an account's password when the email
     * is associated with an existing account. A token is created and sent in an email
     * as a link to open the reset password form.
     */
    @Test
    void validateEmail_enterValidEmail_returnEmptyMessage() {
        String email = resetUser.getEmail();
        String expectedMessage = "";

        String result = forgotPasswordService.validateEmail(email, locale);
        assertEquals(expectedMessage, result);

        List<VerificationCode> verificationCode = (List<VerificationCode>) verificationCodeRepository.findAll();
        assertEquals(resetUser, verificationCode.get(0).getUser());
    }

    /**
     * Tests validating the email used to reset an account's password when the email
     * isn't associated with an existing account. A token is not created.
     */
    @Test
    void validateEmail_enterNotExistsEmail_returnEmptyMessage() {
        String email = "john@doe.com";
        String expectedMessage = "";

        String result = forgotPasswordService.validateEmail(email, locale);
        assertEquals(expectedMessage, result);

        List<VerificationCode> verificationCode = (List<VerificationCode>) verificationCodeRepository.findAll();
        assertTrue(verificationCode.isEmpty());
    }

    /**
     * Tests validating the email used to reset an account's password when the email's
     * format is invalid. A token is not created and an error message is returned.
     */
    @Test
    void validateEmail_enterInvalidEmail_returnErrorMessage() {
        String email = "jane@@doe.com";
        String expectedMessage = "Email address must be in the form ‘jane@doe.nz’.";

        String result = forgotPasswordService.validateEmail(email, locale);
        assertEquals(expectedMessage, result);

        List<VerificationCode> verificationCode = (List<VerificationCode>) verificationCodeRepository.findAll();
        assertTrue(verificationCode.isEmpty());
    }

    /**
     * Test updating a user's password when given a new password and user. The new password
     * will be encoded before replacing the user's old password.
     */
    @Test
    @Transactional
    void updatePasswords_enterUserAndPassword_updateUserPassword() {
        String newPassword = "Test123!";
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        forgotPasswordService.updatePassword(resetUser, newPassword);

        User currentUser = userRepository.findByEmailIgnoreCase(resetUser.getEmail()).orElse(null);
        assertNotNull(currentUser);
        assertTrue(passwordEncoder.matches(newPassword, currentUser.getPassword()));
    }

    /**
     * Test validating a user's new password when both passwords are the same and are in
     * the correct format. No error messages will be returned.
     */
    @Test
    void validatePassword_validPasswords_returnEmptyList() {
        String newPassword = "Test123!";
        String retypePassword = "Test123!";

        List<String> errors = forgotPasswordService.validatePasswords(newPassword, retypePassword, resetUser);

        assertTrue(errors.isEmpty());
    }

    /**
     * Test validating a user's new password when both passwords are not the same and
     * are not in the correct format. An error message will be returned for each mistake.
     */
    @Test
    void validatePassword_invalidAndDifferentPasswords_returnAllErrorsList() {
        String newPassword = "Password";
        String retypePassword = "password";
        List<String> expectedErrors = List.of("The passwords do not match.",
                "Your password must be at least 8 characters long and include at least one " +
                        "uppercase letter, one lowercase letter, one number, and one special character.");

        List<String> errors = forgotPasswordService.validatePasswords(newPassword, retypePassword, resetUser);

        assertEquals(expectedErrors, errors);
    }

    @Test
    void validatePassword_userNameInPassword_returnsError() {
        String newPassword = "Jane1!2foo";
        String confirmPassword = "Jane1!2foo";
        String expectedError = "Your password should not contain your name or email address.";
        List<String> actualErrors = forgotPasswordService.validatePasswords(
            newPassword, confirmPassword, resetUser
        );
        assertEquals(1, actualErrors.size());
        assertEquals(expectedError, actualErrors.getFirst());
    }
}

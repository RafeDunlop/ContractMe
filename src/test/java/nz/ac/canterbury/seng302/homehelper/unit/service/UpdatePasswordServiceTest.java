package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.UpdatePasswordService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class UpdatePasswordServiceTest {
    private static UpdatePasswordService updatePasswordService;
    private static UserRepository userRepositoryMock;
    private static UpdatePasswordDTO updatePasswordDTO;
    private static PasswordEncoder passwordEncoder;
    private static User testUser;

    /**
     * Mocks required classes for constructor of UpdatePasswordService
     * Creates a testUser and populates with valid details
     * Mocks getUserByEmail to return the testUser
     */
    @BeforeEach
    void Setup() {
        LoginService loginServiceMock = Mockito.mock(LoginService.class);
        UserValidation userValidation = new UserValidation();
        userRepositoryMock = Mockito.mock(UserRepository.class);
        EmailService emailServiceMock = Mockito.mock(EmailService.class);

        updatePasswordService = new UpdatePasswordService(userValidation, loginServiceMock, userRepositoryMock, emailServiceMock);
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String encodedPassword = passwordEncoder.encode("Test123!");

        testUser = new User("John", "Doe", "BigJohn55@test.com", encodedPassword);
        when(loginServiceMock.getUserByEmail()).thenReturn(testUser);
    }

    /**
     * Creates a DTO with valid form inputs
     * Sets the testUsers password back to default
     */
    @BeforeEach
    void initializeDTO() {
        updatePasswordDTO =  new UpdatePasswordDTO("Test123!", "Test1234!", "Test1234!");
        testUser.setPassword(passwordEncoder.encode("Test123!"));
    }

    /**
     * Tests with new password and retyped new password empty
     */
    @Test
    void test_update_password_empty_passwords() {
        updatePasswordDTO.setNewPassword("");
        updatePasswordDTO.setRetypePassword("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });

        assertTrue(exception.getMessage().contains("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."));
    }

    /**
     * Blue sky scenario with all valid form details
     */
    @Test
    void test_update_password_blue_sky() {
        updatePasswordService.updatePassword(updatePasswordDTO);
        assertTrue(passwordEncoder.matches("Test1234!", testUser.getPassword()));
        Mockito.verify(userRepositoryMock, Mockito.times(1)).save(testUser);
    }

    /**
     * Tests with the users current password wrong, but valid new and retyped password
     */
    @Test
    void test_update_password_wrong_current_password() {
        updatePasswordDTO.setCurrentPassword("Test1234567!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });

        assertTrue(exception.getMessage().contains("Old Password does not match."));
        Mockito.verify(userRepositoryMock, Mockito.never()).save(testUser);
    }

    /**
     * Tests with new password and retyped new password valid but different
     */
    @Test
    void test_update_password_retyped_password_wrong() {
        updatePasswordDTO.setNewPassword("Test!12345");
        updatePasswordDTO.setRetypePassword("Typo!12345");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });

        assertTrue(exception.getMessage().contains("New Passwords do not match."));
        Mockito.verify(userRepositoryMock, Mockito.never()).save(testUser);
    }

    /**
     * Tests with new password and retyped new password the same but too short for requirements
     */
    @Test
    void test_update_password_too_short_password() {
        updatePasswordDTO.setNewPassword("Test1!");
        updatePasswordDTO.setRetypePassword("Test1!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });

        assertTrue(exception.getMessage().contains("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."));
        Mockito.verify(userRepositoryMock, Mockito.never()).save(testUser);
    }

    /**
     * Tests with new password and retyped new password null
     */
    @Test
    void test_update_password_null_passwords() {
        updatePasswordDTO.setNewPassword(null);
        updatePasswordDTO.setRetypePassword(null);

        assertThrows(NullPointerException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });
        Mockito.verify(userRepositoryMock, Mockito.never()).save(testUser);
    }

    /**
     * Tests with new password and retyped new password same but missing uppercase
     */
    @Test
    void test_update_password_missing_uppercase() {
        updatePasswordDTO.setNewPassword("test12345!");
        updatePasswordDTO.setRetypePassword("test12345!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });

        assertTrue(exception.getMessage().contains("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."));
        Mockito.verify(userRepositoryMock, Mockito.never()).save(testUser);
    }

    /**
     * Tests with a range of special characters in the password
     */
    @Test
    void test_update_password_special_characters() {
        updatePasswordDTO.setNewPassword("Test1!@#$%^&*()=+;:.,");
        updatePasswordDTO.setRetypePassword("Test1!@#$%^&*()=+;:.,");

        updatePasswordService.updatePassword(updatePasswordDTO);
        Mockito.verify(userRepositoryMock, Mockito.times(1)).save(testUser);
    }
}

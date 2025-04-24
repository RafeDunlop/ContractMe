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

    @BeforeEach
    void initializeDTO() {
        updatePasswordDTO =  new UpdatePasswordDTO("Test123!", "Test1234!", "Test1234!");
        testUser.setPassword(passwordEncoder.encode("Test123!"));
    }

    @Test
    void test_update_password_empty_passwords() {
        updatePasswordDTO.setNewPassword("");
        updatePasswordDTO.setRetypePassword("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });

        assertTrue(exception.getMessage().contains("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."));
    }

    @Test
    void test_update_password_blue_sky() {
        updatePasswordService.updatePassword(updatePasswordDTO);
        assertTrue(passwordEncoder.matches("Test1234!", testUser.getPassword()));
        Mockito.verify(userRepositoryMock, Mockito.times(1)).save(testUser);
    }

    @Test
    void test_update_password_wrong_current_password() {
        updatePasswordDTO.setCurrentPassword("Test1234567!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });

        assertTrue(exception.getMessage().contains("Your old password is incorrect."));
        Mockito.verify(userRepositoryMock, Mockito.never()).save(testUser);
    }

    @Test
    void test_update_password_retyped_password_wrong() {
        updatePasswordDTO.setNewPassword("Test!12345");
        updatePasswordDTO.setRetypePassword("Typo!12345");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });

        assertTrue(exception.getMessage().contains("The new passwords do not match."));
        Mockito.verify(userRepositoryMock, Mockito.never()).save(testUser);
    }

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

    @Test
    void test_update_password_null_passwords() {
        updatePasswordDTO.setNewPassword(null);
        updatePasswordDTO.setRetypePassword(null);

        assertThrows(NullPointerException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });
        Mockito.verify(userRepositoryMock, Mockito.never()).save(testUser);
    }

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

    @Test
    void test_update_password_special_characters() {
        updatePasswordDTO.setNewPassword("Test1!@#$%^&*()=+;:.,");
        updatePasswordDTO.setRetypePassword("Test1!@#$%^&*()=+;:.,");

        updatePasswordService.updatePassword(updatePasswordDTO);
        Mockito.verify(userRepositoryMock, Mockito.times(1)).save(testUser);
    }
}

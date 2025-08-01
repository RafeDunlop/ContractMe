package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.UpdatePasswordService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UpdatePasswordServiceTest {
    private static UpdatePasswordService updatePasswordService;
    private static UserRepository userRepositoryMock;
    private static UpdatePasswordDTO updatePasswordDTO;
    private static PasswordEncoder passwordEncoder;
    private static User testUser;

    @BeforeEach
    void Setup() {
        LoginService loginServiceMock = mock(LoginService.class);
        UserValidation userValidation = new UserValidation();
        userRepositoryMock = mock(UserRepository.class);
        EmailService emailServiceMock = mock(EmailService.class);

        updatePasswordService = new UpdatePasswordService(userValidation, loginServiceMock, userRepositoryMock, emailServiceMock);
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String encodedPassword = passwordEncoder.encode("Test123!");

        testUser = new User("John", "Doe", "BigJohn55@test.com", encodedPassword);
        when(loginServiceMock.getUserByEmail()).thenReturn(testUser);
    }

    @BeforeEach
    void initializeDTO() {
        updatePasswordDTO = new UpdatePasswordDTO("Test123!", "Test1234!", "Test1234!");
        testUser.setPassword(passwordEncoder.encode("Test123!"));
    }

    @Test
    void testUpdatePassword_emptyPasswords_error() {
        updatePasswordDTO.setNewPassword("");
        updatePasswordDTO.setRetypePassword("");

        Map<String, java.util.List<String>> errors = updatePasswordService.updatePasswordValidation(updatePasswordDTO);

        assertTrue(errors.containsKey("newPasswordError"));
        assertFalse(errors.isEmpty());
        verify(userRepositoryMock, never()).save(testUser);
    }

    @Test
    void testUpdatePassword_blueSky_noErrorPasswordUpdated() {
        Map<String, java.util.List<String>> errors = updatePasswordService.updatePasswordValidation(updatePasswordDTO);

        assertTrue(errors.isEmpty());

        updatePasswordService.updatePassword(updatePasswordDTO);

        assertTrue(passwordEncoder.matches("Test1234!", testUser.getPassword()));
        verify(userRepositoryMock, times(1)).save(testUser);
    }

    @Test
    void testUpdatePassword_wrongCurrentPassword_error() {
        updatePasswordDTO.setCurrentPassword("WrongPassword!");

        Map<String, java.util.List<String>> errors = updatePasswordService.updatePasswordValidation(updatePasswordDTO);

        assertTrue(errors.containsKey("oldPasswordError"));
        verify(userRepositoryMock, never()).save(testUser);
    }

    @Test
    void testUpdatePassword_retypedPasswordWrong_error() {
        updatePasswordDTO.setNewPassword("Test!12345");
        updatePasswordDTO.setRetypePassword("Typo!12345");

        Map<String, java.util.List<String>> errors = updatePasswordService.updatePasswordValidation(updatePasswordDTO);

        assertTrue(errors.containsKey("newPasswordRetypeError"));
        verify(userRepositoryMock, never()).save(testUser);
    }

    @Test
    void testUpdatePassword_passwordTooShort_error() {
        updatePasswordDTO.setNewPassword("Test1!");
        updatePasswordDTO.setRetypePassword("Test1!");

        Map<String, java.util.List<String>> errors = updatePasswordService.updatePasswordValidation(updatePasswordDTO);

        assertTrue(errors.containsKey("newPasswordError"));
        verify(userRepositoryMock, never()).save(testUser);
    }

    @Test
    void testUpdatePassword_missingUppercase_error() {
        updatePasswordDTO.setNewPassword("test12345!");
        updatePasswordDTO.setRetypePassword("test12345!");

        Map<String, java.util.List<String>> errors = updatePasswordService.updatePasswordValidation(updatePasswordDTO);

        assertTrue(errors.containsKey("newPasswordError"));
        verify(userRepositoryMock, never()).save(testUser);
    }

    @Test
    void testUpdatePassword_specialCharacters_noErrorUpdatedPassword() {
        updatePasswordDTO.setNewPassword("Test1!@#$%^&*()=+;:.,");
        updatePasswordDTO.setRetypePassword("Test1!@#$%^&*()=+;:.,");

        Map<String, java.util.List<String>> errors = updatePasswordService.updatePasswordValidation(updatePasswordDTO);

        assertTrue(errors.isEmpty());

        updatePasswordService.updatePassword(updatePasswordDTO);

        verify(userRepositoryMock, times(1)).save(testUser);
    }
}

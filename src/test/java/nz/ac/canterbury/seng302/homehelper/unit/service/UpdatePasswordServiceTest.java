package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.UpdatePasswordService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class UpdatePasswordServiceTest {

    private static UpdatePasswordService updatePasswordService;
    private static UpdatePasswordDTO updatePasswordDTO;
    private static PasswordEncoder passwordEncoder;

    private static User testUser;

    @BeforeAll
    static void Setup() {
        LoginService loginServiceMock = Mockito.mock(LoginService.class);
        UserValidation userValidationMock = Mockito.mock(UserValidation.class);
        UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);

        updatePasswordService = new UpdatePasswordService(userValidationMock, loginServiceMock, userRepositoryMock);

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
    void test_update_password_null_passwords() {
        updatePasswordDTO.setNewPassword(null);
        updatePasswordDTO.setRetypePassword(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });
    }

    @Test
    void test_update_password_blue_sky() {
        updatePasswordService.updatePassword(updatePasswordDTO);
    }

    @Test
    void test_update_password_wrong_current_password() {
        updatePasswordDTO.setCurrentPassword("Test1234567!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });
    }

    @Test
    void test_update_password_retyped_password_wrong() {
        updatePasswordDTO.setNewPassword("Test!12345");
        updatePasswordDTO.setRetypePassword("Typo!12345");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });
    }

    @Test
    void test_update_password_too_short_password() {
        updatePasswordDTO.setCurrentPassword("Test1234!");
        updatePasswordDTO.setNewPassword("Test123!");
        updatePasswordDTO.setRetypePassword("Test123!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });
        System.out.println(exception);
    }
}

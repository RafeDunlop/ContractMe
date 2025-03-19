package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.dto.UpdatePasswordDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.UpdatePasswordService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class UpdatePasswordServiceIntegrationTest {
    private static UpdatePasswordService updatePasswordService;
    private static UpdatePasswordDTO updatePasswordDTO;
    private static PasswordEncoder passwordEncoder;
    private static LoginService loginServiceMock;
    private static User testUser;

    /**
     * Mocks required classes for constructor of UpdatePasswordService
     * Creates a testUser and populates with valid details
     * Mocks getUserByEmail to return the testUser
     */
    @BeforeAll
    static void Setup() {
        loginServiceMock = Mockito.mock(LoginService.class);
        UserValidation userValidation = new UserValidation();
        UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);

        updatePasswordService = new UpdatePasswordService(userValidation, loginServiceMock, userRepositoryMock);
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
     * Tests users password has been updated
     */
    @Test
    void test_update_password_empty_passwords() {
        updatePasswordDTO.setNewPassword("Test123##");
        updatePasswordDTO.setRetypePassword("Test123##");

        updatePasswordService.updatePassword(updatePasswordDTO);

        assertTrue(passwordEncoder.matches("Test123##", testUser.getPassword()));
    }

    /**
     * Tests the password hasn't been updated
     */
    @Test
    void test_update_password_incorrect_current_password() {
        updatePasswordDTO.setCurrentPassword("Wrong1!@#!");
        updatePasswordDTO.setNewPassword("Test123##");
        updatePasswordDTO.setRetypePassword("Test123##");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updatePasswordService.updatePassword(updatePasswordDTO);
        });

        assertFalse(passwordEncoder.matches("Test123##", testUser.getPassword()));
    }

}

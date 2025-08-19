package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginIntegrationTest {

    @Mock
    private UserRepository userRepository;

    private LoginService loginService;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        UserValidation userValidation = new UserValidation();
        loginService = new LoginService(userRepository, userValidation);
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Test returns a user object (user is logged in) when the inputted email and password are accepted. The returned
     * user object has the same email and password as the inputted strings.
     */
    @Test
    public void getUserByEmailPassword_ValidDetails_ReturnUser() {
        User testUser = new User();
        testUser.setEmail("jane@doe.com");
        testUser.setPassword(passwordEncoder.encode("password"));

        when(userRepository.findByEmailIgnoreCase(testUser.getEmail())).thenReturn(Optional.of(testUser));

        User resultUser = loginService.getUserByEmailAndPassword("jane@doe.com", "password");
        Assertions.assertNotNull(resultUser);
        Assertions.assertEquals(testUser.getEmail(), resultUser.getEmail());
        Assertions.assertEquals(testUser.getPassword(), resultUser.getPassword());
    }

    /**
     * Test throws an error message when the inputted email is not accepted. The returned user object is null and the error
     * message tells the user to use the correct email format.
     */
    @Test
    public void getUserByEmailPassword_InvalidEmail_ReturnError() {
        IllegalArgumentException errorMessage = Assertions.assertThrows(IllegalArgumentException.class,
                () -> loginService.getUserByEmailAndPassword("@jane.doe.com", "password"));
        Assertions.assertTrue(errorMessage.getMessage().contains("Email address must be in the form 'jane@doe.nz'."));
    }

    /**
     * Test throws an error message when the inputted email and password are not accepted. The returned user object is null
     * and the error message tells the user that the email has not been registered or the password is incorrect.
     */
    @Test
    public void getUserByEmailPassword_UnregisteredEmailPassword_ReturnError() {
        when(userRepository.findByEmailIgnoreCase("jane@doe.com")).thenReturn(Optional.empty());

        IllegalArgumentException errorMessage = Assertions.assertThrows(IllegalArgumentException.class,
                () -> loginService.getUserByEmailAndPassword("jane@doe.com", "password"));
        Assertions.assertTrue(errorMessage.getMessage().contains("The email address is unknown, or the password is invalid"));
    }


}

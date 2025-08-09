package nz.ac.canterbury.seng302.homehelper.integration.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;

@SpringBootTest
public class RegisterServiceIntegrationTest {

    private RegisterService registerService;
    private UserRepository userRepositoryMock;

    @MockBean
    private LocationService locationServiceMock;

    @BeforeEach
    void setUp() {
        userRepositoryMock = Mockito.mock(UserRepository.class);
        UserValidation userValidation = new UserValidation();
        registerService = new RegisterService(userRepositoryMock, userValidation, locationServiceMock);
    }

    @Test
    public void testRegister_missingFirstName_notValid() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName(" ");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");

        Map<String, List<String>> errors = registerService.validateRegistration(userRegisterDTO);

        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertTrue(errors.get("firstNameError").contains("First name cannot be empty."));
    }


    @Test
    public void testRegister_longFirstName_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");

        Map<String, List<String>> errors = registerService.validateRegistration(userRegisterDTO);

        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertTrue(errors.get("firstNameError").contains("First name must be 64 characters long or less."));
    }

    @Test
    public void testRegister_validUser_accountCreated() {
        Mockito.when(userRepositoryMock.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");

        Map<String, List<String>> errors = registerService.validateRegistration(userRegisterDTO);

        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    public void testRegister_invalidName_accountNotCreated() {
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase("email@email.com")).thenReturn(Optional.empty());
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("#(*^$&*^&(*&^ æ¿©®™");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");

        Map<String, List<String>> errors = registerService.validateRegistration(userRegisterDTO);

        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertTrue(errors.get("firstNameError").contains("First name must only include letters, spaces, hyphens, or apostrophes."));
    }

    @Test
    public void testRegister_invalidLastName_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("#(*^$&*^&(*&^ æ¿©®™");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");

        Map<String, List<String>> errors = registerService.validateRegistration(userRegisterDTO);

        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertTrue(errors.get("lastNameError").contains("Last name must only include letters, spaces, hyphens, or apostrophes."));
    }

    @Test
    public void testRegister_invalidPassword_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("password");
        userRegisterDTO.setConfirmPassword("password");

        Map<String, List<String>> errors = registerService.validateRegistration(userRegisterDTO);

        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertTrue(errors.get("passwordError").contains("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email)."));
    }

    @Test
    public void testRegister_invalidEmail_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("foo");
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");

        Map<String, List<String>> errors = registerService.validateRegistration(userRegisterDTO);

        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertTrue(errors.get("emailError").contains("Email address must be in the form 'jane@doe.nz'."));
    }

    @Test
    public void testRegister_emailInUse_accountNotCreated() {
        User user = new User();
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase("email@email.com")).thenReturn(Optional.of(user));
        String email = "email@email.com";
        List<String> errors = registerService.validateEmail(email);

        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertTrue(errors.contains("This email address is already in use."));
    }

    @Test
    public void testRegister_passwordsDontMatch_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password2!");
        Map<String, List<String>> errors = registerService.validateRegistration(userRegisterDTO);

        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertTrue(errors.get("confirmPasswordError").contains("Passwords do not match."));
    }
}

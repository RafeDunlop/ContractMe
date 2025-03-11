package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RegisterService;
import nz.ac.canterbury.seng302.homehelper.validation.UserValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class RegisterServiceIntegrationTest {
    private RegisterService registerService;
    private UserRepository userRepositoryMock;

    @BeforeEach
    void setUp() {
        userRepositoryMock = Mockito.mock(UserRepository.class);
        UserValidation userValidation = new UserValidation();
        AuthenticationManager authenticationManagerMock = Mockito.mock(AuthenticationManager.class);
        registerService = new RegisterService(userRepositoryMock, userValidation, authenticationManagerMock);
    }

    @Test
    public void testRegister_missingFirstName_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName(" ");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(userRegisterDTO));
        Assertions.assertEquals("First name cannot be empty.", exception.getMessage());
        Mockito.verify(userRepositoryMock, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testRegister_longFirstName_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(userRegisterDTO));
        Assertions.assertEquals("First name must be 64 characters long or less.", exception.getMessage());
    }

    @Test
    public void testRegister_validUser_accountCreated() {
        Mockito.when(userRepositoryMock.save(Mockito.any())).thenAnswer(i -> i.getArgument(0));
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User expectedUser = new User("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", "Smith", "email@email.com", passwordEncoder.encode("Password1!"));
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");
        User user = Assertions.assertDoesNotThrow(() -> registerService.registerUser(userRegisterDTO));
        Mockito.verify(userRepositoryMock, Mockito.times(1)).save(Mockito.isA(User.class));
        Assertions.assertEquals(expectedUser, user);
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
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(userRegisterDTO));
        Assertions.assertEquals("First name must only include letters, spaces, hyphens, or apostrophes.", exception.getMessage());
        Mockito.verify(userRepositoryMock, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testRegister_invalidLastName_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("#(*^$&*^&(*&^ æ¿©®™");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(userRegisterDTO));
        Assertions.assertEquals("Last name must only include letters, spaces, hyphens, or apostrophes.", exception.getMessage());
        Mockito.verify(userRepositoryMock, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testRegister_invalidPassword_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("password");
        userRegisterDTO.setConfirmPassword("password");
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(userRegisterDTO));
        Assertions.assertEquals("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.", exception.getMessage());
        Mockito.verify(userRepositoryMock, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testRegister_invalidEmail_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("foo");
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(userRegisterDTO));
        Assertions.assertEquals("Email address must be in the form ‘jane@doe.nz’.", exception.getMessage());
        Mockito.verify(userRepositoryMock, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testRegister_emailInUse_accountNotCreated() {
        User user = new User();
        Mockito.when(userRepositoryMock.findByEmailIgnoreCase("email@email.com")).thenReturn(Optional.of(user));
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password1!");
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(userRegisterDTO));
        Assertions.assertEquals("This email address is already in use.", exception.getMessage());
        Mockito.verify(userRepositoryMock, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void testRegister_passwordsDontMatch_accountNotCreated() {
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setEmail("email@email.com");
        userRegisterDTO.setFirstName("John");
        userRegisterDTO.setLastName("Smith");
        userRegisterDTO.setPassword("Password1!");
        userRegisterDTO.setConfirmPassword("Password2!");
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> registerService.registerUser(userRegisterDTO));
        Assertions.assertEquals("Passwords do not match.", exception.getMessage());
        Mockito.verify(userRepositoryMock, Mockito.never()).save(Mockito.any());
    }
}

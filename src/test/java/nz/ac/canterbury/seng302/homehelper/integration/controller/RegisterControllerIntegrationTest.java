package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.RegisterController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.entity.VerificationCode;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.repository.VerificationCodeRepository;
import nz.ac.canterbury.seng302.homehelper.service.EmailService;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RegisterControllerIntegrationTest {
    @Autowired
    private RegisterController registerController;

    /**
     * MockMvc instance used for simulating HTTP requests.
     */
    private MockMvc mockMvc;


    /**
     * Mocked repository to avoid actual database interactions.
     */
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private VerificationCodeRepository verificationCodeRepository;
    @MockBean
    private EmailService emailService;

    /**
     * Initializes the {@link MockMvc} instance with a new setup of the {@link RegisterController}.
     */
    @PostConstruct
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(registerController).build();

    }

    /**
     * Tests the registration of a valid user.
     * This test simulates a user submitting a valid registration form and expects:
     * A redirection (3xx status) upon successful registration.
     * A redirection to .sendVerificationEmail(Mockito.anyString(), Mockito.anyString()),the user profile page.
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testRegisterUser_validUser_success() throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User expectedUser = Mockito.spy(new User("Jane", "Doe", "jane@doe.nz", passwordEncoder.encode("Test123!")));
        Mockito.when(expectedUser.getId()).thenReturn(1L);
        Mockito.when(verificationCodeRepository.save(Mockito.any(VerificationCode.class))).thenAnswer((InvocationOnMock) -> null);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(expectedUser);
        Mockito.when(userRepository.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.empty()).thenReturn(Optional.of(expectedUser));
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("firstName", "Jane")
            .param("lastName", "Doe")
            .param("email", "jane@doe.nz")
            .param("password", "Test123!")
            .param("confirmPassword", "Test123!")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(view().name("redirect:/confirm-registration"));
        verify(emailService, times(1)).sendVerificationEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class));
    }

    /**
     * Tests the registration of an invalid user.sendVerificationEmail(Mockito.anyString(), Mockito.anyString()),
     * This test simulates a user submitting an invalid registration form and expects:
     * A return to the page (200 status) with an error message.
     * First name, Last name and Email should be remembered
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testRegisterUser_invalidUser_fail() throws Exception {
        List<String> expectedErrorList = List.of("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, one special character, and no fields from your profile (like your name or email).");
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane@doe.nz")
                        .param("password", "password")
                        .param("confirmPassword", "password")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/register"))
                .andExpect(flash().attribute("passwordError", expectedErrorList))
                .andExpect(flash().attribute("userRegisterDTO", Matchers.hasProperty("firstName", Matchers.equalTo("Jane"))))
                .andExpect(flash().attribute("userRegisterDTO", Matchers.hasProperty("lastName", Matchers.equalTo("Doe"))))
                .andExpect(flash().attribute("userRegisterDTO", Matchers.hasProperty("email", Matchers.equalTo("jane@doe.nz"))));
        verify(emailService, Mockito.never()).sendVerificationEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class));
    }

    /**
     * Tests the user activation of a valid verification code
     * This test simulates a user submitting a valid verification code on the confirm registration page and expects:
     * A redirect to /login (3xx status).
     * User should be saved
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testConfirmRegistration_validCode_userActivatedAndVerificationCodeDeleted() throws Exception {
        User mockUser = mock(User.class);
        String testCode = "123456";
        Locale testLocale = Locale.ENGLISH;
        VerificationCode verificationCode = new VerificationCode(mockUser, testCode, testLocale);

        Mockito.when(verificationCodeRepository.findByCode(testCode)).thenReturn(Optional.of(verificationCode));

        mockMvc.perform(MockMvcRequestBuilders.post("/confirm-registration")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("code", testCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login"))
                .andExpect(flash().attribute("loginMessage", "Your account has been activated, please log in"));


        verify(mockUser, times(1)).activate();
        verify(verificationCodeRepository, times(1)).delete(verificationCode);
    }

    @Test
    public void testConfirmRegistration_invalidCode_userNotActivated() throws Exception {
        User mockUser = mock(User.class);
        String testCode = "123456";
        String expectedError = "Signup code invalid";

        Mockito.when(verificationCodeRepository.findByCode(testCode)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/confirm-registration")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("code", testCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(redirectedUrl("/confirm-registration"))
                .andExpect(flash().attribute("errorMessage", expectedError));

        verify(verificationCodeRepository, times(1)).findByCode(testCode);
        verify(mockUser, never()).activate();
        verify(userRepository, never()).save(mockUser);
        verify(verificationCodeRepository, never()).delete(Mockito.any(VerificationCode.class));
    }

}

package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class LoginControllerIntegrationTest {


    /**
     * MockMvc instance used for simulating HTTP requests.
     */
    @Autowired
    private MockMvc mockMvc;


    /**
     * Mocked repository to avoid actual database interactions.
     */
    @MockBean
    private UserRepository userRepository;

    /**
     * Tests the login of a valid user.
     * This test simulates a user submitting a valid login form for a user that exists in the repository
     * Expects:
     * A redirection to the home page
     *
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testLoginUser_userWithSameEmailAndPasswordInRepository_success() throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User expectedUser = Mockito.spy(new User("Jane", "Doe", "jane@doe.nz", passwordEncoder.encode("Test123!")));
        expectedUser.activate();
        Mockito.when(userRepository.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.of(expectedUser));
        mockMvc.perform(formLogin("/login")
                        .user("username", "jane@doe.nz")
                        .password("Test123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }

    /**
     * Tests the login with an incorrect password but correct email.
     * This test simulates a user submitting an invalid login form with the right email but wrong password.
     * Expects:
     * return to form with error message
     *
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testLoginUser_userWithSameEmailInRepositoryWrongPassword_fail() throws Exception {
        String expectedError = "The email address is unknown, or the password is invalid.";
        Mockito.when(userRepository.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.empty());
        mockMvc.perform(formLogin("/login")
                        .user("username", "jane@doe.nz")
                        .password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(request().sessionAttribute("errorMessage", expectedError));
    }

    @Test
    public void testLogout_userLoggedIn_logoutSuccessful() throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User expectedUser = new User("John", "Smith", "john@smith.nz", passwordEncoder.encode("Test123!"));
        expectedUser.activate();

        Mockito.when(userRepository.findByEmailIgnoreCase("john@smith.nz")).thenReturn(Optional.of(expectedUser));
        mockMvc.perform(formLogin("/login")
                        .user("username", "john@smith.nz")
                        .password("Test123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));

        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}


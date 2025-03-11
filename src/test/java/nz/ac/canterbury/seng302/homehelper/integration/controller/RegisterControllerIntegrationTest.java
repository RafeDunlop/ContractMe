package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.controller.RegisterController;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@SpringBootTest
@AutoConfigureMockMvc
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
     * A redirection to the user profile page.
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testRegisterUser_validUser_success() throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User expectedUser = Mockito.spy(new User("Jane", "Doe", "jane@doe.nz", passwordEncoder.encode("Test123!")));
        expectedUser.grantAuthority("ROLE_USER");
        Mockito.when(expectedUser.getId()).thenReturn(1L);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(expectedUser);
        Mockito.when(userRepository.findByEmailIgnoreCase(Mockito.anyString())).thenReturn(Optional.empty()).thenReturn(Optional.of(expectedUser));;
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("firstName", "Jane")
            .param("lastName", "Doe")
            .param("email", "jane@doe.nz")
            .param("password", "Test123!")
            .param("confirmPassword", "Test123!")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(view().name("redirect:/user"));
    }

    /**
     * Tests the registration of an invalid user.
     * This test simulates a user submitting an invalid registration form and expects:
     * A return to the page (200 status) with an error message.
     * First name, Last name and Email should be remembered
     * @throws Exception if the request processing fails.
     */
    @Test
    public void testRegisterUser_invalidUser_fail() throws Exception {
        List<String> expectedErrorList = List.of("Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.");
        mockMvc.perform(MockMvcRequestBuilders.post("/register")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("firstName", "Jane")
                        .param("lastName", "Doe")
                        .param("email", "jane@doe.nz")
                        .param("password", "password")
                        .param("confirmPassword", "password")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(view().name("registration"))
                .andExpect(model().attribute("errorMessages", expectedErrorList))
                .andExpect(model().attribute("firstName", "Jane"))
                .andExpect(model().attribute("lastName", "Doe"))
                .andExpect(model().attribute("email", "jane@doe.nz"));
    }

}

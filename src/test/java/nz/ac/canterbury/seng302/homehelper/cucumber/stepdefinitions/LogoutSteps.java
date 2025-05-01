package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.*;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest
public class LogoutSteps {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    private MvcResult loginResult;

    @Given("I am logged in as a user")
    public void i_am_logged_in_as_a_user() throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User user = new User("John", "Smith", "john@smith.nz", encoder.encode("Test123!"));
        user.activate();

        when(userRepository.save(user)).thenReturn(user);


        // Only mock the needed method
        when(userRepository.findByEmailIgnoreCase("john@smith.nz")).thenReturn(Optional.of(user));

        loginResult = mockMvc.perform(formLogin("/login")
                        .user("username", "john@smith.nz")
                        .password("Test123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"))
                .andReturn();
    }

    @When("I click the logout button")
    public void i_send_a_logout_request() throws Exception {
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        loginResult = mockMvc.perform(post("/logout")
                        .with(csrf())
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Then("I should be redirected to the login page")
    public void i_should_be_redirected_to_the_login_page() {
        String location = loginResult.getResponse().getRedirectedUrl();
        Assertions.assertTrue(location.contains("/login"));
    }

    @Then("I should not be able to access protected pages")
    public void i_should_not_be_able_to_access_protected_pages() throws Exception {
        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/main")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}

package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.JoinTeamController;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "jane@doe.com")
public class JoinTeamControllerIntegrationTest {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    private JoinTeamController teamController;


    private MockHttpSession session;



    @BeforeEach
    public void setupUser() {
        User currentUser = new User("Jane", "Doe", "jane@doe.com", "password");
        currentUser.grantAuthority("ROLE_USER");
        userRepository.save(currentUser);

        session = new MockHttpSession();
    }

    @Test
    public void teamController_hasLocationOwnsRecord_getsForm() throws Exception {
        MvcResult result = mockMvc.perform(get("/join-team")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(result.getResponse().getContentAsString().contains("Renovation Name"));
        assertTrue(result.getResponse().getContentAsString().contains("Role"));

    }
}

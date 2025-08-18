package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.RequestInboxController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestInboxControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestInboxController requestInboxController;

    @MockBean
    private TeamsRepository teamsRepository;

    @MockBean
    private UserRepository userRepository;

    private static Contractor contractor;

    private static User user;

    private static RenovationRecord renovationRecord;

    @BeforeAll
    static void setUp() {
        contractor = new Contractor("Jane", "Doe", "jane@doe.com", "password");
        user = new User("John", "Doe", "john@doe.com", "password");
        renovationRecord = new RenovationRecord(user, "Record 1", "description", List.of());
    }

    @Test
    @WithMockUser("jane@doe.com")
    void requestInbox_userIsContractor_returnsRequestInboxTemplateWithTeams() throws Exception {
        Team team = new Team(renovationRecord);
        Role role = new Role(contractor, Skill.HVAC, false);
        team.addRole(role);

        Mockito.when(teamsRepository.findByRoleContractor(contractor)).thenReturn(List.of(team));
        Mockito.when(userRepository.findByEmailIgnoreCase(contractor.getEmail())).thenReturn(Optional.of(contractor));

        mockMvc.perform(get("/view-requests"))
                .andExpect(status().isOk())
                .andExpect(view().name("requestInboxTemplate"))
                .andExpect(model().attribute("teams", List.of(team)));
    }

    @Test
    @WithMockUser("john@doe.com")
    void requestInbox_userIsNotContractor_returnMainRedirect() throws Exception {
        Mockito.when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/view-requests"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }
}

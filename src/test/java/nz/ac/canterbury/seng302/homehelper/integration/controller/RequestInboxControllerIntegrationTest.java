package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.controller.RequestInboxController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.*;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class RequestInboxControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestInboxController requestInboxController;

    @Autowired
    private ContractorRepository contractorRepository;

    @MockBean
    private TeamsRepository teamsRepository;

    @MockBean
    private UserRepository userRepository;

    private Contractor contractor;

    private User user;

    private RenovationRecord renovationRecord;

    @BeforeEach
    void setUp() {
        contractor = new Contractor("Jane", "Doe", "jane@doe.com", "password");
        contractor = contractorRepository.save(contractor);
        user = new User("John", "Doe", "john@doe.com", "password");
        renovationRecord = new RenovationRecord(user, "Record 1", "description", List.of());
    }

    @Test
    @WithMockUser("jane@doe.com")
    void requestInbox_userIsContractor_returnsRequestInboxTemplateWithTeams() throws Exception {
        Team team = new Team(renovationRecord);
        Role role = new Role(contractor, Skill.HVAC,  RoleStatus.WAITING);
        team.addRole(role);
        when(teamsRepository.findByRoleContractor(1L)).thenReturn(List.of(team));
        when(userRepository.findByEmailIgnoreCase(contractor.getEmail())).thenReturn(Optional.of(contractor));

        mockMvc.perform(get("/view-requests"))
                .andExpect(status().isOk())
                .andExpect(view().name("requestInboxTemplate"))
                .andExpect(model().attribute("teams", List.of(team)));
    }

    @Test
    @WithMockUser("john@doe.com")
    void requestInbox_userIsNotContractor_returnMainRedirect() throws Exception {
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/view-requests"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }
}

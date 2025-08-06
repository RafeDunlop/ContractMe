package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.homehelper.controller.TeamController;
import nz.ac.canterbury.seng302.homehelper.dto.TeamRequestDTO;
import nz.ac.canterbury.seng302.homehelper.dto.TeamRoleDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "jane@doe.nz")
@Transactional
public class TeamControllerIntegrationTest {

    @Autowired
    private TeamController teamController;

    @Autowired
    private MockMvc mockMvc;

    private User user;

    private RenovationRecord renovationRecord;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TeamsService teamsService;

    @Autowired
    private TeamsRepository teamsRepository;
    @Autowired
    private ObjectMapper objectMapper;

    private TestInfo testInfo;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        user = new User("Jane", "Doe", "jane@doe.nz", "password");
        user = userRepository.save(user);
        renovationRecord = new RenovationRecord(user, "test renovation", "test description", List.of());
        renovationRecord = renovationRecordRepository.save(renovationRecord);

        if (testInfo.getDisplayName().contains("hasLocation")) {
            Location location = new Location();
            location.setAddress("nonNull");
            renovationRecord.setLocation(location);
            renovationRecordRepository.save(renovationRecord);
        }

    }

    @Test
    public void teamController_hasLocationOwnsRecord_getsForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "different@user.nz")
    public void teamController_hasLocationDoesNotOwnRecord_returns404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void teamController_ownsRecordDoesNotHaveLocation_returns404() throws Exception {
        Location location = new Location();
        renovationRecord.setLocation(location);
        renovationRecordRepository.save(renovationRecord);
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTeam_submitsTeamWithRolesAndHasLocation_createsTeam() throws Exception {
        TeamRequestDTO teamRequestDTO = new TeamRequestDTO();
        teamRequestDTO.setRenovationRecordId(renovationRecord.getId());
        TeamRoleDTO role1 = new TeamRoleDTO();
        role1.setSkill(Skill.CARPENTRY);
        teamRequestDTO.setRoles(List.of(role1));


        mockMvc.perform(MockMvcRequestBuilders.post("/renovations/team/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamRequestDTO))
                        .with(csrf()))
                .andExpect(status().isOk());

        boolean exists = teamsRepository.existsByRenovationRecordId(renovationRecord.getId());
        assertTrue(exists);
        assertEquals(1,teamsRepository.findByRenovationRecord(renovationRecord).getRoles().size());

    }

    @Test
    public void createTeam_renovationHasTeamAndHasLocation_returns404() throws Exception {
        Team existingTeam = new Team(renovationRecord);
        teamsRepository.save(existingTeam);
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().isNotFound());
    }


}

package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.TeamController;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @BeforeEach
    public void setup() {
        user = new User("Jane", "Doe", "jane@doe.nz", "password");
        user = userRepository.save(user);
        renovationRecord = new RenovationRecord(user, "test renovation", "test description", List.of());
        renovationRecord = renovationRecordRepository.save(renovationRecord);
    }

    @Test
    public void teamController_hasLocationOwnsRecord_getsForm() throws Exception {
        Location location = new Location();
        location.setAddress("nonNull");
        renovationRecord.setLocation(location);
        renovationRecordRepository.save(renovationRecord);

        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "different@user.nz")
    public void teamController_hasLocationDoesNotOwnRecord_returns404() throws Exception {
        Location location = new Location();
        renovationRecord.setLocation(location);
        location.setAddress("nonNull");
        renovationRecordRepository.save(renovationRecord);
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
}

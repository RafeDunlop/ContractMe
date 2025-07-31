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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Optional;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class TeamControllerIntegrationTest {
    @Autowired
    private TeamController teamController;

    private MockMvc mockMvc;
    private User user;
    private RenovationRecord renovationRecord;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private RenovationRecordRepository renovationRecordRepository;

    @BeforeEach
    public void setup() {
        user = new User("Jane", "Doe", "jane@doe.nz", "password");
        when(userRepository.findByEmailIgnoreCase("jane@doe.nz")).thenReturn(Optional.of(user));
        renovationRecord = spy(new RenovationRecord());
        when(renovationRecord.getId()).thenReturn(1L);
        when(renovationRecordRepository.findById(1L)).thenReturn(Optional.of(renovationRecord));
    }

    @Test
    @WithMockUser(username = "jane@doe.nz")
    public void teamController_hasLocationOwnsRecord_getsForm() throws Exception {
        Location location = new Location();
        renovationRecord.setLocation(location);
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")).andExpect(status().isOk());
    }
}

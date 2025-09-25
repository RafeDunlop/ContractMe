package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org. springframework. test. web. servlet. request. MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "johnny.maps@gmail.com")
class MapControllerIntegrationTest {

    private ObjectMapper mapper;

    private long idFirst;

    private long idSecond;

    private long idThird;

    private Contractor contractor;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TeamsRepository teamsRepository;

    @BeforeEach
    void beforeEach () {
        User loggedIn = new User(
                "Johnny",
                "Maps",
                "johnny.maps@gmail.com",
                "dummyPassword"
        );
        loggedIn = userRepository.save(loggedIn);
        User notLoggedIn = new User(
                "Jimmy",
                "No-Maps",
                "jimmy.nomaps@gmail.com",
                "dummyPassword"
        );
        notLoggedIn = userRepository.save(notLoggedIn);

        renovationRecordRepository.deleteAll();
        idFirst = registerRecord(loggedIn, false, 0d, 0d).getId();
        idSecond = registerRecord(loggedIn, false, 4.999d, 0d).getId();
        idThird = registerRecord(notLoggedIn, true, 2d, 2d).getId();
        registerRecord(notLoggedIn, false, 3d, 3d);
        mapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() {
        renovationRecordRepository.deleteAll();
        userRepository.deleteAll();
        teamsRepository.deleteAll();
    }

    private RenovationRecord registerRecord(User user, boolean publicity, double latitude, double longitude) {
        RenovationRecord renovation = new RenovationRecord(
                user,
                "name",
                "description",
                List.of()
        );
        renovation.setLocation(new Location(
                "street address",
                "country",
                "postcode",
                "city",
                "suburb",
                latitude,
                longitude
        ));
        renovation.setPublicity(publicity);
        return renovationRecordRepository.save(renovation);
    }

    private void createContractors() {
        contractor = new Contractor("Jane", "Doe", "jane@doe.com", "password");
        contractor.setSkills(Set.of(Skill.ACOUSTIC_INSULATION));
        contractor.setLocation(new Location("77 Ilam Rd", "New Zealand", "8041", "Christchurch", "Ilam"));
        contractor = userRepository.save(contractor);
    }

    @Test
    void getRenovationsInBounds_privateValidRectangle_getsPrivateMappings() throws Exception {
        MvcResult result = mockMvc.perform(get("/map/renovations/0D,0D,40D,40D")
                    .param("withPublic", "false"))
                .andExpect(status().isOk())
                .andReturn();
        List<MappedRenovation> resultCaptive = mapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(2, resultCaptive.size());
        assertEquals(idFirst, resultCaptive.get(0).getId());
        assertEquals(idSecond, resultCaptive.get(1).getId());
    }

    @Test
    void getRenovationsInBounds_publicValidRectangleAndImplicitPublicityInclusion_getsAllMappings() throws Exception {
        MvcResult result = mockMvc.perform(get("/map/renovations/0D,0D,40D,40D"))
                .andExpect(status().isOk())
                .andReturn();
        List<MappedRenovation> resultCaptive = mapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(3, resultCaptive.size());
        assertEquals(idFirst, resultCaptive.get(0).getId());
        assertEquals(idSecond, resultCaptive.get(1).getId());
        assertEquals(idThird, resultCaptive.get(2).getId());
    }

    @Test
    void getRenovationsInBounds_publicValidRectangleAndExplicitPublicity_getsAllMappings() throws Exception {
        MvcResult result = mockMvc.perform(get("/map/renovations/0D,0D,40D,40D")
                        .param("withPublic", "true"))
                .andExpect(status().isOk())
                .andReturn();
        List<MappedRenovation> resultCaptive = mapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(3, resultCaptive.size());
        assertEquals(idFirst, resultCaptive.get(0).getId());
        assertEquals(idSecond, resultCaptive.get(1).getId());
        assertEquals(idThird, resultCaptive.get(2).getId());
    }

    @Test
    void getRenovationsInBounds_publicAndPointRectangle_getsOnlyExactMatch() throws Exception {
        MvcResult result = mockMvc.perform(get("/map/renovations/0D,0D,0D,0D"))
                .andExpect(status().isOk())
                .andReturn();
        List<MappedRenovation> resultCaptive = mapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(1, resultCaptive.size());
        assertEquals(idFirst, resultCaptive.get(0).getId());
    }

    @Test
    void getRenovationsInBounds_rectangleIllegal_noMatches() throws Exception {
        MvcResult result = mockMvc.perform(get("/map/renovations/10D,10D,10D,10D"))
                .andExpect(status().isOk())
                .andReturn();
        List<MappedRenovation> resultCaptive = mapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>(){});
        assertTrue(resultCaptive.isEmpty());
    }

    @Test
    @WithMockUser("jimmy.nomaps@gmail.com")
    void getContractorByRenovationId_userNotInTeam_returnException() throws Exception {
        RenovationRecord renovationRecord = renovationRecordRepository.findById(idFirst).orElse(null);
        Team team = teamsRepository.save(new Team(renovationRecord));

        mockMvc.perform(get("/map/contractors?id=" + team.getId()))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"johnny.maps@gmail.com", "jane@doe.com"})
    void getContractorByRenovationId_userIsInTeam_returnContractors(String userEmail) throws Exception {
        createContractors();
        RenovationRecord renovationRecord = renovationRecordRepository.findById(idFirst).orElse(null);
        Team team = new Team(renovationRecord);
        Role role = new Role(Skill.ANTIQUE_RESTORATION);
        role.setContractor(contractor);
        team.addRole(role);
        team = teamsRepository.save(team);

        mockMvc.perform(get("/map/contractors?id=" + team.getId())
                        .with(user(userEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value(contractor.getFullName()))
                .andExpect(jsonPath("$[0].email").value(contractor.getEmail()))
                .andExpect(jsonPath("$[0].skill").value(role.getSkill().name()));
    }
}

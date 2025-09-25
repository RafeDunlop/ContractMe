package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.dto.MappedContractor;
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
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org. springframework. test. web. servlet. request. MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "johnny.maps@gmail.com")
public class MapControllerIntegrationTest {

    private ObjectMapper mapper;

    private long idFirst;

    private long idSecond;

    private long idThird;
    private RenovationRecord recordFirst;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ContractorRepository contractorRepository;
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
        recordFirst = registerRecord(loggedIn, false, 0d, 0d);
        idFirst = recordFirst.getId();
        idSecond = registerRecord(loggedIn, false, 4.999d, 0d).getId();
        idThird = registerRecord(notLoggedIn, true, 2d, 2d).getId();
        registerRecord(notLoggedIn, false, 3d, 3d);
        mapper = new ObjectMapper();
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

    private Contractor registerContractor(String email, boolean available, Set<Skill> skills, double latitude, double longitude) {
        Contractor contractor = new Contractor("Bobby", "Tables", email, "password");
        contractor.setAvailable(available);
        contractor.setSkills(skills);
        contractor.setLocation(new Location(
                "street address",
                "country",
                "postcode",
                "city",
                "suburb",
                latitude,
                longitude
        ));
        return contractorRepository.save(contractor);
    }

    @Test
    void getRenovationsInBounds_privateValidRectangle_getsPrivateMappings() throws Exception {
        MvcResult result = mockMvc.perform(get("/map/renovations")
                        .param("minLat", Double.toString(0d))
                        .param("minLon", Double.toString(0d))
                        .param("maxLat", Double.toString(40d))
                        .param("maxLon", Double.toString(40d))
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
        MvcResult result = mockMvc.perform(get("/map/renovations")
                .param("minLat", Double.toString(0d))
                .param("minLon", Double.toString(0d))
                .param("maxLat", Double.toString(40d))
                .param("maxLon", Double.toString(40d)))
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
        MvcResult result = mockMvc.perform(get("/map/renovations")
                        .param("minLat", Double.toString(0d))
                        .param("minLon", Double.toString(0d))
                        .param("maxLat", Double.toString(40d))
                        .param("maxLon", Double.toString(40d))
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
        MvcResult result = mockMvc.perform(get("/map/renovations")
                        .param("minLat", Double.toString(0d))
                        .param("minLon", Double.toString(0d))
                        .param("maxLat", Double.toString(0d))
                        .param("maxLon", Double.toString(0d))
                        .param("withPublic", "true"))
                .andExpect(status().isOk())
                .andReturn();
        List<MappedRenovation> resultCaptive = mapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(1, resultCaptive.size());
        assertEquals(idFirst, resultCaptive.get(0).getId());
    }

    @Test
    void getRenovationsInBounds_rectangleIllegal_noMatches() throws Exception {
        MvcResult result = mockMvc.perform(get("/map/renovations")
                        .param("minLat", Double.toString(10d))
                        .param("minLon", Double.toString(10d))
                        .param("maxLat", Double.toString(10d))
                        .param("maxLon", Double.toString(10d)))
                .andExpect(status().isOk())
                .andReturn();
        List<MappedRenovation> resultCaptive = mapper.readValue(
                result.getResponse().getContentAsString(), new TypeReference<>(){});
        assertTrue(resultCaptive.isEmpty());
    }

    @Test
    void getRenovationCoords_validId_returnsValidCoordinates() throws Exception {
        mockMvc.perform(get("/map/renovation")
                .param("id", String.valueOf(idFirst)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(0d))
                .andExpect(jsonPath("$.longitude").value(0d))
                .andReturn();

    }

    @Test
    void getRenovationCoords_invalidId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/map/renovation")
                .param("id", String.valueOf(99999)))
                .andExpect(status().isNotFound());
    }



    @Test
    void getEligibleContractors_contractorsInBounds_returnsCorrectList() throws Exception {
        Contractor contractor = registerContractor("bob@contractor.nz", true, Set.of(Skill.ARCHITECTURE), 0.1d, 0.1d);
        Contractor contractor2 = registerContractor("bob@othercontractor.nz", true, Set.of(Skill.ARCHITECTURE, Skill.RESOURCE_CONSENT_COMPLIANCE), 0.1d, -0.1d);
        registerContractor("bob@unavailable.net", false, Set.of(Skill.ARCHITECTURE), 0.123d, 0.132d);
        registerContractor("bob@farawayplace.com", true, Set.of(Skill.ARCHITECTURE), -15.03, 48.82);
        registerContractor("bob@notarchitecture.co.nz", true, Set.of(Skill.ASBESTOS_REMOVAL), 0.1, 0.1);
        Team team = new Team(recordFirst);
        team.addRole(new Role(Skill.ARCHITECTURE));
        team = teamsRepository.save(team);
        List<MappedContractor> expectedContractors = Stream.of(contractor, contractor2).map(c -> new MappedContractor(c, Skill.ARCHITECTURE)).toList();
        MvcResult result = mockMvc.perform(get("/map/eligible")
                .param("skill", "ARCHITECTURE")
                .param("teamId", String.valueOf(team.getId()))).andReturn();
        List<MappedContractor> resultCaptive = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(expectedContractors, resultCaptive);
    }

    @Test
    void getEligibleContractors_noContractors_returnsEmptyList() throws Exception {
        Team team = new Team(recordFirst);
        team.addRole(new Role(Skill.ARCHITECTURE));
        team = teamsRepository.save(team);
        MvcResult result = mockMvc.perform(get("/map/eligible")
                .param("skill", "ARCHITECTURE")
                .param("teamId", String.valueOf(team.getId()))).andReturn();
        List<MappedContractor> actualContractors = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(0, actualContractors.size());
    }
}

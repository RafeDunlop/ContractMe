package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private MockMvc mockMvc;

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
                        .param("maxLon", Double.toString(0d)))
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
}

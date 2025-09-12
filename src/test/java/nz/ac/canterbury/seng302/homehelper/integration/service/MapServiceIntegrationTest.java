package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.dto.CoordinateRectangle;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.MapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class MapServiceIntegrationTest {

    @Autowired
    private MapService toTest;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @MockBean
    private LoginService loginService;

    @Autowired
    private UserRepository userRepository;

    private User loggedIn;

    private User notLoggedIn;

    @BeforeEach
    void beforeEach() {
        loggedIn = new User(
                "Johnny",
                "Maps",
                "johnny.maps@gmail.com",
                "dummyPassword"
        );
        loggedIn = userRepository.save(loggedIn);
        notLoggedIn = new User(
                "Jimmy",
                "No-Maps",
                "jimmy.nomaps@gmail.com",
                "dummyPassword"
        );
        notLoggedIn = userRepository.save(notLoggedIn);
        when(loginService.getUserByEmail()).thenReturn(loggedIn);

    }

    private RenovationRecord registerRecord(User user, boolean publicity, double latitude, double longitude) {
        RenovationRecord record = new RenovationRecord(
                user,
                "name",
                "description",
                List.of()
        );
        record.setLocation(new Location(
                "street address",
                "country",
                "postcode",
                "city",
                "suburb",
                latitude,
                longitude
        ));
        record.setPublicity(publicity);
        return renovationRecordRepository.save(record);
    }

    @Test
    void getRenovationsInBounds_noPublicAndValidCoords_getsPrivateOnly() {
        CoordinateRectangle rectangle = new CoordinateRectangle(
                0d,
                0d,
                5d,
                5d
        );
        registerRecord(loggedIn, false, 0d, 0d);
        registerRecord(loggedIn, false, 4.999d, 0d);
        registerRecord(notLoggedIn, true, 2d, 2d);
        List<MappedRenovation> resultCaptive = toTest.getRenovationsInBounds(rectangle, false);
        assertEquals(2, resultCaptive.size());
        assertEquals(1, resultCaptive.get(0).getId());
        assertEquals(2, resultCaptive.get(1).getId());
    }

    @Test
    void getRenovationsInBounds_publicAllValidCoords_getsPrivateAndPublic() {
        CoordinateRectangle rectangle = new CoordinateRectangle(
                0d,
                0d,
                5d,
                5d
        );
        registerRecord(loggedIn, false, 0d, 0d);
        registerRecord(loggedIn, false, 4.999d, 0d);
        registerRecord(notLoggedIn, true, 2d, 2d);
        registerRecord(notLoggedIn, false, 3d, 3d);
        List<MappedRenovation> resultCaptive = toTest.getRenovationsInBounds(rectangle, true);
        assertEquals(3, resultCaptive.size());
        assertEquals(1, resultCaptive.get(0).getId());
        assertEquals(2, resultCaptive.get(1).getId());
        assertEquals(3, resultCaptive.get(2).getId());
    }

    @Test
    void getRenovationsInBounds_publicAndRectangleIsPoint_getsExactMatchOnly() {
        CoordinateRectangle rectangle = new CoordinateRectangle(
                0d,
                0d,
                0d,
                0d
        );
        registerRecord(loggedIn, false, 0d, 0d);
        registerRecord(loggedIn, false, 4.999d, 0d);
        registerRecord(notLoggedIn, true, 2d, 2d);
        registerRecord(notLoggedIn, false, 3d, 3d);
        List<MappedRenovation> resultCaptive = toTest.getRenovationsInBounds(rectangle, true);
        assertEquals(1, resultCaptive.size());
        assertEquals(1, resultCaptive.get(0).getId());
    }

    @Test
    void getRenovationsInBounds_rectangleIllegal_noMatches() {
        CoordinateRectangle rectangle = new CoordinateRectangle(
                1d,
                1d,
                0d,
                0d
        );
        registerRecord(loggedIn, false, 0d, 0d);
        registerRecord(loggedIn, false, 4.999d, 0d);
        registerRecord(notLoggedIn, true, 2d, 2d);
        registerRecord(notLoggedIn, false, 3d, 3d);
        List<MappedRenovation> resultCaptive = toTest.getRenovationsInBounds(rectangle, true);
        assertTrue(resultCaptive.isEmpty());
    }

}

package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.dto.CoordinateRectangle;
import nz.ac.canterbury.seng302.homehelper.dto.MappedRenovation;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.MapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class MapServiceTest {

    private MapService toTest;

    private User loggedIn;

    private RenovationRecordRepository repository;

    @BeforeEach
    public void beforeEach() {
        loggedIn = Mockito.mock(User.class);
        repository = Mockito.mock(RenovationRecordRepository.class);
        LoginService loginService = Mockito.mock(LoginService.class);
        when(loginService.getUserByEmail()).thenReturn(loggedIn);
        toTest = new MapService(repository, loginService);
    }

    private RenovationRecord mockRecord(long id) {
        RenovationRecord record = Mockito.mock(RenovationRecord.class);
        when(record.getId()).thenReturn(id);
        when(record.getLocation()).thenReturn(Mockito.mock(Location.class));
        return record;
    }

    @Test
    void getRenovationsInBounds_noPublic_getsExactlyPrivate() {
        CoordinateRectangle rectangle = new CoordinateRectangle(
                0d,
                0d,
                1d,
                1d
        );
        RenovationRecord privateOwned = mockRecord(0);
        RenovationRecord publicOwned = mockRecord(1);
        RenovationRecord publicNotOwned = mockRecord(2);
        when(repository.findOwnedWithinBox(loggedIn,
                rectangle.getMinLat(),
                rectangle.getMinLat(),
                rectangle.getMaxLat(),
                rectangle.getMaxLon())
        ).thenReturn(
                List.of(privateOwned, publicOwned)
        );
        when(repository.findPublicWithinBox(
                rectangle.getMinLat(),
                rectangle.getMinLat(),
                rectangle.getMaxLat(),
                rectangle.getMaxLon())
        ).thenReturn(
                List.of(publicOwned, publicNotOwned)
        );
        List<MappedRenovation> resultCaptive = toTest.getRenovationsInBounds(rectangle, false);
        assertEquals(2, resultCaptive.size());
        assertEquals(0, resultCaptive.get(0).getId());
        assertEquals(1, resultCaptive.get(1).getId());
    }

    @Test
    void getRenovationsInBounds_public_getsAllAndOneCopy() {
        CoordinateRectangle rectangle = new CoordinateRectangle(
                0d,
                0d,
                1d,
                1d
        );
        RenovationRecord privateOwned = mockRecord(0);
        RenovationRecord publicOwned = mockRecord(1);
        RenovationRecord publicNotOwned = mockRecord(2);
        when(repository.findOwnedWithinBox(loggedIn,
                rectangle.getMinLat(),
                rectangle.getMinLat(),
                rectangle.getMaxLat(),
                rectangle.getMaxLon())
        ).thenReturn(
                List.of(privateOwned, publicOwned)
        );
        when(repository.findPublicWithinBox(
                rectangle.getMinLat(),
                rectangle.getMinLat(),
                rectangle.getMaxLat(),
                rectangle.getMaxLon())
        ).thenReturn(
                List.of(publicOwned, publicNotOwned)
        );
        List<MappedRenovation> resultCaptive = toTest.getRenovationsInBounds(rectangle, true);
        assertEquals(3, resultCaptive.size());
        assertEquals(0, resultCaptive.get(0).getId());
        assertEquals(1, resultCaptive.get(1).getId());
        assertEquals(2, resultCaptive.get(2).getId());
    }

}

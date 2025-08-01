package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.config.Keys;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class LocationServiceTest {
    private LocationService locationService;

    @BeforeEach
    public void setUp() {
        Keys keys = Mockito.mock(Keys.class);
        LocationValidation locationValidation = Mockito.mock(LocationValidation.class);
        locationService = new LocationService(keys, locationValidation);
    }

    @Test
    public void testHasLocation_locationValid_returnsTrue() {
        Location location = new Location("Jack Erskine", "", "", "", "");
        RenovationRecord renovationRecord = new RenovationRecord();
        renovationRecord.setLocation(location);
        assertTrue(locationService.hasLocation(renovationRecord));
    }

    @Test
    public void testHasLocation_locationNull_returnsFalse() {
        RenovationRecord renovationRecord = new RenovationRecord();
        assertFalse(locationService.hasLocation(renovationRecord));
    }

    @Test
    public void testHasLocation_addressEmpty_returnsFalse() {
        RenovationRecord renovationRecord = new RenovationRecord();
        Location location = new Location("", "", "", "", "");
        renovationRecord.setLocation(location);
        assertFalse(locationService.hasLocation(renovationRecord));
    }
}

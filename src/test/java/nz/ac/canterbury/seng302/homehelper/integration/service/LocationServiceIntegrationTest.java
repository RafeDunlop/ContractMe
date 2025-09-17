package nz.ac.canterbury.seng302.homehelper.integration.service;

import nz.ac.canterbury.seng302.homehelper.config.Keys;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class LocationServiceIntegrationTest {

    private LocationService locationService;
    private Keys keys;
    private LocationValidation locationValidation;

    @BeforeEach
    void setUp() {
        LocationService locationService = new LocationService(keys, locationValidation);
    }

}

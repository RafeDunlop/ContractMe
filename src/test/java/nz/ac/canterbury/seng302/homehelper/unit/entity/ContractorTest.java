package nz.ac.canterbury.seng302.homehelper.unit.entity;

import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class ContractorTest {

    private Contractor contractor;

    @BeforeEach
    void setUp() {
        contractor = new Contractor();
    }

    @Test
    void getPhoneNumberFormatted_validFields_outputIsReadable() {
        contractor.setCountryCode(64);
        contractor.setPhoneNumber("12 345 6789");
        assertEquals("+64 12 345 6789", contractor.getPhoneNumberFormatted());
    }

    @Test
    void getHourlyRateFormatted_validLocaleRate_outputIsReadable() {
        contractor.setHourlyRate(23.7f);
        assertEquals("$23.70", contractor.getHourlyRateFormatted(Locale.US));
    }

    @Test
    void getHourlyRateFormatted_validLocaleRateZero_outputIsReadable() {
        contractor.setHourlyRate(0f);
        assertTrue(contractor.getHourlyRateFormatted(Locale.GERMANY).contains("€"));
    }
}

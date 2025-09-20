package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.config.Keys;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.service.LocationNotFoundException;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocationServiceTest {

    private LocationService locationService;
    private LocationService locationServiceSpy;

    @BeforeEach
    public void setup() {
        Keys keys = Mockito.mock(Keys.class);
        LocationValidation locationValidation = Mockito.mock(LocationValidation.class);
        locationService = new LocationService(keys, locationValidation);
        locationServiceSpy = Mockito.spy(locationService);
    }

    private static Stream<Arguments> streamValidLocationInputsWithCoordinates() {
        return Stream.of(
                Arguments.of("1 Address", "Suburb", "City", "1111", "Country", 45D, 90D),
                Arguments.of("2 Address", "Suburb", "City", "", "", -45D, -90D),
                Arguments.of("3 Address", "", "", "1111", "", 0.001D, -0.001D),
                Arguments.of("4 Address", "", "", "", "Country", 100D, -100D),
                Arguments.of("5 Address", "", "", "", "", 0.999D, -0.999D)
        );
    }

    private AddressDTO setAddressDTOValues(String address, String suburb, String city, String postcode, String country) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1(address);
        addressDTO.setRegion(suburb);
        addressDTO.setCity(city);
        addressDTO.setPostcode(postcode);
        addressDTO.setCountry(country);
        return addressDTO;
    }

    @ParameterizedTest
    @MethodSource("streamValidLocationInputsWithCoordinates")
    void locate_inputValidLocationsWithCoordinates_returnLocationWithGivenDetails(String address, String suburb, String city,
                                                                                  String postcode, String country, Double lat,
                                                                                  Double lon) {
        Location expectedLocation = new Location(address, country, postcode, city, suburb, lat, lon);
        AddressDTO inputtedAddressDTO = setAddressDTOValues(address, suburb, city, postcode, country);
        inputtedAddressDTO.setLat(lat);
        inputtedAddressDTO.setLon(lon);
        doNothing().when(locationServiceSpy).injectCoordsViaGeocoding(inputtedAddressDTO);
        Location actualLocation = locationServiceSpy.locate(inputtedAddressDTO);
        verify(locationServiceSpy, times(1)).injectCoordsViaGeocoding(inputtedAddressDTO);
        Assertions.assertEquals(expectedLocation, actualLocation);
    }

    @Test
    void locate_inputValidLocationWithoutCoordinates_returnLocationWithGeolocatedDetails() {
        String address = "1 Address";
        String suburb = "Suburb";
        String city = "City";
        String postcode = "1111";
        String country = "Country";
        double lat = 1D;
        double lon = 1D;
        Location expectedLocation = new Location(address, country, postcode, city, suburb, lat, lon);
        AddressDTO inputtedAddressDTO = setAddressDTOValues(address, suburb, city, postcode, country);

        // Prevent call to API and instead add coordinates to DTO when injectCoordsViaGeocoding is called.
        Mockito.doAnswer(invocationOnMock -> {
            AddressDTO mockAddressDTO = invocationOnMock.getArgument(0);
            mockAddressDTO.setLat(lat);
            mockAddressDTO.setLon(lon);
            return null;
        }).when(locationServiceSpy).injectCoordsViaGeocoding(Mockito.any(AddressDTO.class));

        Location actualLocation = locationServiceSpy.locate(inputtedAddressDTO);

        Assertions.assertEquals(expectedLocation, actualLocation);
    }

    @Test
    void locate_inputInvalidLocation_throwsException() {
        String address = "Fake Address";
        String suburb = "Fake Suburb";
        String city = "Fake City";
        String postcode = "0000";
        String country = "Fake Country";
        AddressDTO inputtedAddressDTO = setAddressDTOValues(address, suburb, city, postcode, country);

        // Throw an exception to simulate the API failing to find coordinates for the inputted location.
        Mockito.doThrow(IllegalArgumentException.class).when(locationServiceSpy).injectCoordsViaGeocoding(Mockito.any(AddressDTO.class));

        LocationNotFoundException exception = assertThrows(LocationNotFoundException.class, () -> locationServiceSpy.locate(inputtedAddressDTO));
        assertEquals("The address could not be found", exception.getMessage());

    }

    @Test
    void testHasLocation_locationValid_returnsTrue() {
        Location location = new Location("Jack Erskine", "", "", "", "");
        RenovationRecord renovationRecord = new RenovationRecord();
        renovationRecord.setLocation(location);
        assertTrue(locationService.hasLocation(renovationRecord));
    }

    @Test
    void testHasLocation_locationNull_returnsFalse() {
        RenovationRecord renovationRecord = new RenovationRecord();
        assertFalse(locationService.hasLocation(renovationRecord));
    }

    @Test
    void testHasLocation_addressEmpty_returnsFalse() {
        RenovationRecord renovationRecord = new RenovationRecord();
        Location location = new Location("", "", "", "", "");
        renovationRecord.setLocation(location);
        assertFalse(locationService.hasLocation(renovationRecord));
    }
}
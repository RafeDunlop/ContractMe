package nz.ac.canterbury.seng302.homehelper.unit.validation;

import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class LocationValidationTest {

    public static Stream<String> getValidSuburb() {
        return Stream.of("Riccarton", "Upper-Riccarton", "Taylor's Mistake", "Ilam1", "");
    }

    @ParameterizedTest
    @MethodSource("getValidSuburb")
    public void LocationValidation_ValidSuburbName_InputAccepted(String suburb) {
        LocationValidation locationValidation = new LocationValidation();
        Assertions.assertTrue(locationValidation.validateSuburb(suburb).isEmpty());
    }

    public static Stream<String> getInvalidSuburb() {
        return Stream.of("r*ccarton", ",", "!@#$%^&*()");
    }

    @ParameterizedTest
    @MethodSource("getInvalidSuburb")
    public void LocationValidation_InvalidSuburbName_InputNotAccepted(String suburb) {
        LocationValidation locationValidation = new LocationValidation();
        Assertions.assertFalse(locationValidation.validateSuburb(suburb).isEmpty());
    }


}

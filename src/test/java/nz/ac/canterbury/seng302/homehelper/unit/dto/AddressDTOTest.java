package nz.ac.canterbury.seng302.homehelper.unit.dto;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AddressDTOTest {

    @ParameterizedTest
    @ValueSource(strings = {"21 Kirkwood Ave\nINFO 1234 --- [home-helper] : Malicious log entry",
            "21 Kirkwood Ave\rINFO 1234 --- [home-helper] : Malicious log entry"})
    void getLoggedAddress_addressContainsNewLines_newLinesStripped(String address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1(address);
        Assertions.assertEquals("21 Kirkwood Ave_INFO 1234 --- [home-helper] : Malicious log entry", addressDTO.getLoggedAddress());
    }

    @Test
    void getLoggedAddress_addressContainsMultipleNewLines_newLinesStripped() {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1("21 Kirkwood Ave\r\nINFO 1234 --- [home-helper] : Malicious log entry");
        Assertions.assertEquals("21 Kirkwood Ave__INFO 1234 --- [home-helper] : Malicious log entry", addressDTO.getLoggedAddress());
    }
}

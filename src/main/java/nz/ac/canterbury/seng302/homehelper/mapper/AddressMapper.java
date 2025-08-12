package nz.ac.canterbury.seng302.homehelper.mapper;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressDTO mapLocationToAddressDTO(Location location) {
        if (location == null) {
            return null;
        }
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1(location.getAddress());
        addressDTO.setCountry(location.getCountry());
        addressDTO.setPostcode(location.getPostcode());
        addressDTO.setCity(location.getCity());
        addressDTO.setRegion(location.getSuburb());
        addressDTO.setLat(location.getLatitude());
        addressDTO.setLon(location.getLongitude());
        return addressDTO;
    }
}
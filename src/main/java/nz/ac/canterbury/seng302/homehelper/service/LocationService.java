package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.dto.LocationDTO;
import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationService {

    private final LocationValidation locationValidation;

    @Autowired
    public LocationService(LocationValidation locationValidation) {
        this.locationValidation = locationValidation;
    }

    public Map<String, List<String>> validateLocation(LocationDTO dto) {
        return new HashMap<String, List<String>>();
    }
}

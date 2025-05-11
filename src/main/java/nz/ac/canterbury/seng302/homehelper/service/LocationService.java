package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final LocationValidation locationValidation;

    @Autowired
    public LocationService(LocationValidation locationValidation) {
        this.locationValidation = locationValidation;
    }
}

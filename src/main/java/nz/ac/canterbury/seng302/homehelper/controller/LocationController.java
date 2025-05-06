package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.LocalisationDTO;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final LocationService locationService;

    /**
     * Controller for initialising Location retrieval
     * @param locationService The location service to provide backend functionality
     */
    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * Gets the localisation information corresponding to the ip address of the client
     * @return The localisation of the client, packaged into a {@link LocalisationDTO} object
     */
    @GetMapping("/localisation")
    public ResponseEntity<LocalisationDTO> getLocalisation(HttpServletRequest request) {
        String ipAddress = getIpFromRequest(request);
        // validation via service/in following call etc.
        try {
            return new ResponseEntity<>(locationService.getRoughLocation(ipAddress), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves a list of autocomplete suggestions, ordered by relevance, based on the specified localisation information
     * using Geoapify
     * @param prompt The partial address
     * @param localisationDTO The localisation information packaged. Must contain {@code country.iso_code} as well as
     *                        {@code location.latitude} and {@code location.longitude}
     * @return An ordered list of autocompletion suggestions
     */
    @GetMapping("/address-autocomplete/{prompt}")
    public ResponseEntity<List<AddressDTO>> getAddressAutoComplete(@PathVariable String prompt,
                                                   @ModelAttribute LocalisationDTO localisationDTO) {
        // validation via service/in following call etc.
        try {
            return new ResponseEntity<>(locationService.getAutocomplete(prompt, localisationDTO), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private String getIpFromRequest(HttpServletRequest request) {
        String forwardingHeader = request.getHeader("X-Forwarded-For");
        if (forwardingHeader == null || forwardingHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return forwardingHeader.split(",")[0];
    }



}

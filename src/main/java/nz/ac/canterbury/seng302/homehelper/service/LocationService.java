package nz.ac.canterbury.seng302.homehelper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.homehelper.config.Keys;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.LocalisationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Basic unlinked location implementation using Geoapify
 */
@Service
public class LocationService {

    private static final String LOCALHOST_IP = "127.0.0.1";

    private static final String GEOAPIFY_BASE_URL = "https://api.geoapify.com/v1/";

    private static final String IP_API = "ipinfo";

    private final Keys keys;

    private static final String AUTOCOMPLETE_API = "geocode/autocomplete";

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    @Autowired
    public LocationService(Keys keys) {
        this.keys = keys;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    /**
     * Retrieves the localisation of a client from their ip address
     * @param ip The ip address of the client machine
     */
    public LocalisationDTO getRoughLocation(String ip) {
        ResponseEntity<String> response = restTemplate.getForEntity(getIpGrabUrl(ip), String.class);
        logger.trace(response.getBody());
        if (response.getStatusCode() != HttpStatus.OK) {
            String errorMessage = String.format("could not retrieve localisation information. Response code: %s", response.getStatusCode());
            logger.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            return objectMapper.readValue(response.getBody(), LocalisationDTO.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Retrieves address autocomplete suggestions based on the specified prompt and sorted by the specified localisation information
     * @param prompt The partial address input for which to retrieve autocomplete suggestions
     * @param localisationDTO contains fields whose union defines the localisation of the request for biasing request results
     * @return a list of address objects with relevant fields, as retrieved from the API
     */
    public List<AddressDTO> getAutocomplete(String prompt, LocalisationDTO localisationDTO) {
        if (localisationDTO.getCountry() == null || localisationDTO.getLocation() == null) {
            logger.warn("Required localisation information is missing");
            throw new IllegalArgumentException();
        }
        ResponseEntity<String> response = restTemplate.getForEntity(getAutoCompleteUrl(
                prompt,
                localisationDTO.getCountry().getIso_code(),
                localisationDTO.getLocation().getLatitude(),
                localisationDTO.getLocation().getLongitude()
        ), String.class);
        logger.trace(response.getBody());
        if (response.getStatusCode() != HttpStatus.OK) {
            String errorMessage = String.format("could not retrieve address autocomplete suggestions. Response code: %s", response.getStatusCode());
            logger.warn(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.get("results");
            return objectMapper.readValue(results.toString(), new TypeReference<>() {});
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }
    public Map<String, List<String>> validateLocation(AddressDTO dto) {
        return new HashMap<String, List<String>>();
    }

    /**
     * checks if the location has been provided
     * @param dto location data transfer object
     * @return true if location has been provided, false if it hasn't
     */
    public boolean isLocationProvided(AddressDTO dto) {
        return dto != null &&
                Stream.of(dto.getAddress_line1(), dto.getCountry(), dto.getPostcode(), dto.getCity(), dto.getRegion())
                        .anyMatch(field -> field != null && !field.trim().isEmpty());
    }


    /**
     * Assembles the URL to call the Geoapify autocomplete API endpoint for the specified parameters
     * @param prompt The incomplete address to provide suggestions for
     * @param countryCode The iso-code for this country, e.g. de for germany, nz for New Zealand
     * @param latitude The lateral position on earth in degrees from the prime meridian
     * @param longitude The vertical position on earth in degrees from the equator
     * @return The full url to call
     */
    private String getAutoCompleteUrl(String prompt, String countryCode, double latitude, double longitude) {
        for (String input : List.of(prompt, countryCode, String.valueOf(latitude), String.valueOf(longitude))) {
            if (input == null || input.isEmpty()) {
                logger.warn("A required input is missing");
                throw new IllegalArgumentException();
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(GEOAPIFY_BASE_URL);
        sb.append(AUTOCOMPLETE_API);
        sb.append(String.format("?text=%s", prompt));
        sb.append(String.format("&filter=countrycode:%s", countryCode));
        sb.append(String.format("&bias=proximity:%f,%f", latitude, longitude));
        sb.append("&type=street");
        sb.append("&lang=en");
        sb.append("&format=json");
        logger.debug("calling autocomplete API: {}", sb);
        sb.append(String.format("&apiKey=%s", keys.getGeoapify()));
        return URLEncoder.encode(sb.toString(), Charset.defaultCharset());
    }

    /**
     * Gets the fully qualified URL to submit to the Geoapify IP Geolocation API
     * @param ip The IP address of the client. If it corresponds to localhost, it will be omitted from the request URL,
     *           and the IP address of the hosting machine will be used
     * @return The full url to call
     */
    private String getIpGrabUrl(String ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(GEOAPIFY_BASE_URL);
        sb.append(IP_API);
        sb.append("?");
        if (!ip.equals(LOCALHOST_IP)) sb.append(String.format("ip=%s", ip)); // use request ip instead if localhost
        logger.debug("calling IP grab API: {}", sb);
        sb.append(String.format("&apiKey=%s", keys.getGeoapify()));
        return sb.toString();
    }
}

package nz.ac.canterbury.seng302.homehelper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.LocalisationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * Basic unlinked location implementation using Geoapify
 * @author Rafe Dunlop
 */
@Service
public class LocationService {

    private static final String geoapifyBaseUrl = "https://api.geoapify.com/v1/";

    private static final String ipApi = "ipinfo";

    private static final String autocompleteApi = "geocode/autocomplete";

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    public LocationService() {
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


    private String getAutoCompleteUrl(String prompt, String countryCode, double latitude, double longitude) {
        for (String input : List.of(prompt, countryCode, String.valueOf(latitude), String.valueOf(longitude))) {
            if (input == null || input.isEmpty()) {
                logger.warn("A required input is missing");
                throw new IllegalArgumentException();
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(geoapifyBaseUrl);
        sb.append(autocompleteApi);
        sb.append(String.format("?text=%s", prompt));
        sb.append(String.format("&filter=countrycode:%s", countryCode));
        sb.append(String.format("&bias=proximity:%f,%f", latitude, longitude));
        sb.append("&type=street");
        sb.append("&lang=en");
        sb.append("&format=json");
        sb.append(String.format("&apiKey=%s", System.getenv("GEOAPIFY_API_KEY")));
        logger.debug("calling autocomplete API: {}", sb);
        return sb.toString();
    }

    private String getIpGrabUrl(String ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(geoapifyBaseUrl);
        sb.append(ipApi);
        sb.append(String.format("?ip=%s&apiKey=%s", ip, System.getenv("GEOAPIFY_API_KEY")));
        logger.debug("calling IP grab API: {}", sb);
        return sb.toString();
    }
}

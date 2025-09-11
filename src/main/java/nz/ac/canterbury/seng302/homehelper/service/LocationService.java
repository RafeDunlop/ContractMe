package nz.ac.canterbury.seng302.homehelper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.config.Keys;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.GeocodingCoordsDTO;
import nz.ac.canterbury.seng302.homehelper.dto.LocalisationDTO;
import nz.ac.canterbury.seng302.homehelper.util.MapUtil;
import nz.ac.canterbury.seng302.homehelper.validation.LocationValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Basic unlinked location implementation using Geoapify
 */
@Service
public class LocationService {

    private static final String LOCALHOST_IP_IPV4 = "127.0.0.1";

    private static final String LOCALHOST_IP_IPV6 = "0:0:0:0:0:0:0:1";

    private static final String GEOAPIFY_BASE_URL = "https://api.geoapify.com/v1/";

    private static final double CONFIDENCE_LEVEL = 0.95d;

    private static final String IP_API = "ipinfo";

    private final Keys keys;

    private static final String AUTOCOMPLETE_API = "geocode/autocomplete";

    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    private final LocationValidation locationValidation;

    @Autowired
    public LocationService(Keys keys, LocationValidation locationValidation) {
        this.keys = keys;
        this.locationValidation = locationValidation;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    /**
     * This method calls Geoapify API endpoints. These should be mocked for testing.
     * If the addressDTO is empty, this will still create and return an empty Location without co-ordinates in order to
     * maintain consistent data
     * @param addressDTO address data object passed from frontend
     * @return fully-formed {@link Location} object guaranteed to be supplied coordinates
     * @throws LocationNotFoundException if valid coordinates could not be found for the location
     */
    public Location locate(AddressDTO addressDTO) throws LocationNotFoundException {
        if (!hasCoords(addressDTO) && isLocationProvided(addressDTO)) {
            try {
                injectCoordsViaGeocoding(addressDTO);
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to acquire location coordinates via geocoding: {}", e.getMessage());
                throw new LocationNotFoundException("Please enter a valid address", e);
            }
        }
        String loggedAddress = addressDTO.getLoggedAddress();
        logger.info("Creating location for address {} with coords {}, {}",
                loggedAddress,
                addressDTO.getLat(),
                addressDTO.getLon()
        );
        return new Location(
                addressDTO.getAddress_line1(),
                addressDTO.getCountry(),
                addressDTO.getPostcode(),
                addressDTO.getCity(),
                addressDTO.getRegion(),
                addressDTO.getLat(),
                addressDTO.getLon()
        );
    }

    /**
     * Attempts to inject the co-ordinates of a custom-input address via the Geoapify Geocoding service.
     * @param addressDTO The {@link AddressDTO} to be edited inline with the co-ordinates supplied by Geoapify
     * @throws IllegalArgumentException if the specified address does not exist or is not present or if Geoapify
     * cannot identify coordinates for it
     */
    public void injectCoordsViaGeocoding(AddressDTO addressDTO) throws IllegalArgumentException {
        String loggedAddress = addressDTO.getLoggedAddress();
        logger.debug("Attempting to retrieve coordinates via geocoding for address {}", loggedAddress);
        ResponseEntity<String> response = restTemplate.getForEntity(getGeocodingCompleteUrl(addressDTO), String.class);
        logger.debug(response.getBody());
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode results = root.get("results");
            List<GeocodingCoordsDTO> coordResultList = objectMapper.readValue(results.toString(), new TypeReference<>() {});
            if (coordResultList.isEmpty()) {
                throw new IllegalArgumentException("No coordinates found");
            } else if (coordResultList.get(0).getConfidence() < CONFIDENCE_LEVEL) {
                throw new IllegalArgumentException("No results with satisfactory confidence found");
            } else {
                addressDTO.setLat(coordResultList.get(0).getLat());
                addressDTO.setLon(coordResultList.get(0).getLon());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse geocoding results", e);
        }
    }

    /**
     * Injects the co-ordinates of a malformed custom-input address via Geoapify's IP geolocation service. This is
     * less precise than geolocation or autocomplete
     * @param addressDTO The {@link AddressDTO} to be edited inline with the co-ordinates supplied by Geoapify
     * @param ipAddress The ip address of the request to be forwarded to Geopaify
     */
    public void injectCoordsViaIpGeolocation(AddressDTO addressDTO, String ipAddress) {
        logger.info("Retrieving rough coordinates for address {} via request ip: {}",
                addressDTO.getAddress_line1(),
                ipAddress
        );
        LocalisationDTO localisationDTO = getRoughLocation(ipAddress);
        addressDTO.setLat(localisationDTO.getLocation().getLatitude());
        addressDTO.setLon(localisationDTO.getLocation().getLongitude());
    }

    /**
     * Retrieves the localisation of a client from their ip address
     * @param ip The ip address of the client machine
     * @return The localisation of the client identified by their IP address packaged into a DTO object
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
            return injectSuburbs(objectMapper.readValue(results.toString(), new TypeReference<>() {}));
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }


    /**
     * Runs validation on each of the user input params
     * @param dto the addressdto containing the user inputted location data
     * @return map of errors
     */
    public Map<String, List<String>> validateLocation(AddressDTO dto) {
        Map<String, List<String>> errors = new HashMap<>();

        boolean locationProvided = Stream.of(dto.getRegion(), dto.getCity(), dto.getPostcode(), dto.getCountry())
                .anyMatch(field -> field != null && !field.isBlank());

        MapUtil.putIfNotEmpty(errors, "addressError", locationValidation.validateStreetAddress(dto.getAddress_line1(), locationProvided));
        MapUtil.putIfNotEmpty(errors, "suburbError", locationValidation.validateSuburb(dto.getRegion()));
        MapUtil.putIfNotEmpty(errors, "cityError", locationValidation.validateCity(dto.getCity()));
        MapUtil.putIfNotEmpty(errors,"postcodeError", locationValidation.validatePostcode(dto.getPostcode()));
        MapUtil.putIfNotEmpty(errors, "countryError", locationValidation.validateCountry(dto.getCountry()));

        return errors;
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
     * Gets whether a location is provided by a renovation record
     * @param record The renovation record to check
     * @return Whether a location is provided by a renovation record
     */
    public boolean hasLocation(RenovationRecord record) {
        Location location = record.getLocation();
        return location != null && location.getAddress() != null && !location.getAddress().isEmpty();
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
                throw new IllegalArgumentException("A required input is missing");
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(GEOAPIFY_BASE_URL);
        sb.append(AUTOCOMPLETE_API);
        sb.append(String.format("?text=%s", URLEncoder.encode(prompt, StandardCharsets.UTF_8)));
        sb.append(String.format("&filter=countrycode:%s", countryCode.toLowerCase()));
        sb.append(String.format("&bias=proximity:%f,%f", latitude, longitude));
        sb.append("&lang=en");
        sb.append("&format=json");
        logger.debug("calling autocomplete API: {}", sb);
        sb.append(String.format("&apiKey=%s", keys.getGeoapify()));
        return sb.toString();
    }

    /**
     * Gets the URL to call to retrieve geocoding information about the custom supplied location
     * @param address The {@link AddressDTO} which contains the address
     * @return The URL to call to retrieve geocoding information about the custom supplied location
     */
    private String getGeocodingCompleteUrl(AddressDTO address) {
        String addressEntry = Stream.of(
                address.getAddress_line1(),
                address.getRegion(),
                address.getCity(),
                address.getPostcode(),
                address.getCountry()
        ).filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(", "));
        StringBuilder sb = new StringBuilder();
        sb.append(GEOAPIFY_BASE_URL);
        sb.append(AUTOCOMPLETE_API);
        sb.append(String.format("?text=%s", URLEncoder.encode(addressEntry, StandardCharsets.UTF_8)));
        sb.append("&lang=en");
        sb.append("&format=json");
        logger.debug("calling geocoding API: {}", sb);
        sb.append(String.format("&apiKey=%s", keys.getGeoapify()));
        return sb.toString();
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
        if (!ip.equals(LOCALHOST_IP_IPV4) && !ip.equals(LOCALHOST_IP_IPV6)) sb.append(String.format("ip=%s", ip)); // use request ip instead if localhost
        logger.debug("calling IP grab API: {}", sb);
        sb.append(String.format("&apiKey=%s", keys.getGeoapify()));
        return sb.toString();
    }

    /**
     * Injects the region field into the addresses provided based on their second address line.
     * "region" is interpreted as suburb
     * @param addresses The addresses for which to set the suburb field
     * @return The addresses specified
     */
    private List<AddressDTO> injectSuburbs(List<AddressDTO> addresses) {
        for (AddressDTO address : addresses) {
            if (address.getAddress_line2() == null || address.getAddress_line2().isEmpty()) {
                continue;
            }
            if (address.getAddress_line2().contains(",")) {
                address.setRegion(address.getAddress_line2().split(",")[0]);
            } else if (address.getAddress_line2().contains(" ")) {
                address.setRegion(address.getAddress_line2().split(" ")[0]);
            } else {
                address.setRegion(address.getAddress_line2());
            }
        }
        return addresses;
    }

    /**
     * Performs a simple values-based check to check whether a location has co-ordinates provided
     * note: 0, 0 is null island (middle of sea). Nobody lives or works there
     * @param address The DTO which contains fields for co-ordinates
     * @return Whether the co-ordinates provided in the specified address are null-equivalent (returns false)
     */
    private boolean hasCoords(AddressDTO address) {
        return !(address.getLon() == 0d ||
                address.getLat() == 0d);
    }

    /**
     * Gets the IP address of the request, principally from the original client that submitted the request
     * if forwarded
     * Uses {@link RequestContextHolder} from Spring to statically extract the web request, via {@link ServletRequestAttributes}
     * @return The IP address of the client
     */
    public String getIpFromRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            String forwardingHeader = request.getHeader("X-Forwarded-For");
            if (forwardingHeader == null || forwardingHeader.isEmpty()) {
                return request.getRemoteAddr();
            }
            return forwardingHeader.split(",")[0];
        }
        throw new IllegalStateException("Method called illegally outside web request context");
    }

    /**
     * Gets an {@link AddressDTO} which represents the merging of an AddressDTO with an existing location.
     * Nullifies co-ordinates if they correspond to the stored Location (so they may be recalculated)
     * @param currentLocation The saved {@link Location}
     * @param editedAddressDTO The edited address fields
     * @return The specified locations merged into one {@link AddressDTO}
     */
    public AddressDTO updateEditedLocation(Location currentLocation, AddressDTO editedAddressDTO) {
        if (currentLocation != null) {
            Location editedLocation = new Location(editedAddressDTO.getAddress_line1(), editedAddressDTO.getCountry(),
                    editedAddressDTO.getPostcode(), editedAddressDTO.getCity(), editedAddressDTO.getRegion(),
                    editedAddressDTO.getLat(), editedAddressDTO.getLon());
            boolean sameCoordinates = currentLocation.getLatitude() == editedLocation.getLatitude() &&
                    currentLocation.getLongitude() == editedLocation.getLongitude();
            if (!Objects.equals(currentLocation, editedLocation) && sameCoordinates) {
                editedAddressDTO.setLat(0L);
                editedAddressDTO.setLon(0L);
            }
        }
        return editedAddressDTO;
    }
}
package nz.ac.canterbury.seng302.homehelper.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Basic unlinked location implementation using iPapi and Google Places
 * @author Rafe Dunlop
 */
public class LocationServiceIpapiPlaces {

    private static final String ipapiBaseUrl = "https://ipapi.co/";

    private static final String placesJsonAutocompleteUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json";

    private static final int locationBiasRadiusMetres = 500000; //500 km

    private static final long placesApiKey = -1; //redacted

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceIpapiPlaces.class);

    /**
     * Gets localisation information using an iPapi API
     * @param ip The IP address to retrieve the localisation of
     * @throws IOException If the request is not successful
     */
    public void getRoughLocation(Long ip) throws IOException {
        URL url = getUrlIpGrab(ip);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "java-ipapi-v1.02");
        logger.info(connection.getResponseMessage());
    }

    /**
     * Retrieves address autocomplete suggestions based on the specified prompt and sorted by the specified localisation information
     * @param prompt The unfinished address input
     * @param countryCode The <a href="https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">ISO 3166</a> 2-letter country code
     * @param latitude The latitude retrieved from an IP address
     * @param longitude The longitude retrieved from an IP address
     * @throws IOException if the request is unsuccessful
     */
    public void getAutocomplete(String prompt, String countryCode, double latitude, double longitude) throws IOException {
        URL url = getAutocompleteUrl(prompt, countryCode, latitude, longitude);;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/json");
        logger.info(connection.getResponseMessage());
    }

    private URL getAutocompleteUrl(String prompt, String countryCode, double latitude, double longitude) {
        StringBuilder sb = new StringBuilder();
        sb.append(placesJsonAutocompleteUrl);
        sb.append(String.format("?input=%s", prompt));
        sb.append(String.format("&location=%f-%f", latitude, longitude));
        sb.append(String.format("&locationbias=%d@%f,%f", locationBiasRadiusMetres, latitude, longitude));
        sb.append(String.format("&components=country:%s", countryCode));
        sb.append(String.format("&key=%d", placesApiKey));
        try {
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            logger.warn(e.getMessage());
            throw new IllegalArgumentException("Malformed URL");
        }
    }

    private URL getUrlIpGrab(Long ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipapiBaseUrl);
        sb.append(String.format("/%d/json/", ip));
        try {
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            logger.warn(e.getMessage());
            throw new IllegalArgumentException("Malformed URL");
        }
    }
}

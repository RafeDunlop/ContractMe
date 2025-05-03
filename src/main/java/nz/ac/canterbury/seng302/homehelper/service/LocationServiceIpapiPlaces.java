package nz.ac.canterbury.seng302.homehelper.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LocationServiceIpapiPlaces {

    private static final String ipapiBaseUrl = "https://ipapi.co/";

    private static final String placesJsonAutocompleteUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json";

    private static final int locationBiasRadiusMetres = 500000; //500 km

    private static final long placesApiKey = -1; //redacted

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceIpapiPlaces.class);

    public void getRoughLocation(Long ip) throws IOException {
        URL url = getUrlIpGrab(ip);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "java-ipapi-v1.02");
        logger.info(connection.getResponseMessage());
    }

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

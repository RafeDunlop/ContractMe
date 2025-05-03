package nz.ac.canterbury.seng302.homehelper.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class LocationServiceGeoapify {

    private static final String geoapifyBaseUrl = "https://api.geoapify.com/v1/";

    private static final String ipApi = "ipinfo";

    private static final String autocompleteApi = "geocode/autocomplete";

    private static final long apiKey = -1; // redacted

    private static final Logger logger = LoggerFactory.getLogger(LocationServiceGeoapify.class);

    public void getRoughLocation(Long ip) throws IOException {
        URL url = getUrlIpGrab(ip);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/json");
        logger.info(connection.getResponseMessage());
    }

    public void getAutocomplete(String prompt, String countryCode, double latitude, double longitude) throws IOException {
        URL url = getAutoCompleteUrl(prompt, countryCode, latitude, longitude);;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Accept", "application/json");
        logger.info(connection.getResponseMessage());
    }


    private URL getAutoCompleteUrl(String prompt, String countryCode, double latitude, double longitude) {
        StringBuilder sb = new StringBuilder();
        sb.append(geoapifyBaseUrl);
        sb.append(autocompleteApi);
        sb.append(String.format("?text=%s", prompt));
        sb.append(String.format("&filter=countrycode:%s", countryCode));
        sb.append(String.format("&bias=proximity:%f,%f", latitude, longitude));
        sb.append(String.format("&apiKey=%d", apiKey));
        sb.append("&type=street");
        sb.append("&lang=en");
        sb.append("&format=json");
        try {
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            logger.warn(e.getMessage());
            throw new IllegalArgumentException("Malformed URL");
        }
    }

    private URL getUrlIpGrab(Long ip) {
        StringBuilder sb = new StringBuilder();
        sb.append(geoapifyBaseUrl);
        sb.append(ipApi);
        sb.append(String.format("?ip=%d&apiKey=%d", ip, apiKey));
        try {
            return new URL(sb.toString());
        } catch (MalformedURLException e) {
            logger.warn(e.getMessage());
            throw new IllegalArgumentException("Malformed URL");
        }
    }

}

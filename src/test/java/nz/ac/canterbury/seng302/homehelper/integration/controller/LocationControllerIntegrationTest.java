package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.config.Keys;
import nz.ac.canterbury.seng302.homehelper.controller.LocationController;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDataAttributionDTO;
import nz.ac.canterbury.seng302.homehelper.dto.CoordsDTO;
import nz.ac.canterbury.seng302.homehelper.dto.CountryDTO;
import nz.ac.canterbury.seng302.homehelper.dto.LocalisationDataAttributionDTO;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class LocationControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private LocationController locationController;

    @Autowired
    private Keys keys;

    @SpyBean
    private LocationService locationService;

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AddressDataAttributionDTO addressAttribution;

    private LocalisationDataAttributionDTO localisationAttribution;

    @PostConstruct
    private void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
        ReflectionTestUtils.setField(locationService, "restTemplate", restTemplate);
    }

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(locationService, "restTemplate", restTemplate);
        keys.setGeoapify("notAnApiKey");
        localisationAttribution = new LocalisationDataAttributionDTO();
        localisationAttribution.setName("IP to City Lite");
        localisationAttribution.setAttribution("<a href='https://db-ip.com'>IP Geolocation by DB-IP</a>");
        localisationAttribution.setLicense("Creative Commons Attribution License");
        addressAttribution = new AddressDataAttributionDTO();
        addressAttribution.setSourcename("openstreetmap");
        addressAttribution.setAttribution("© OpenStreetMap contributors");
        addressAttribution.setLicense("Open Database License");
        addressAttribution.setUrl("https://www.openstreetmap.org/copyright");
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testGetLocalisation_notLocal_getsAllInformation() throws Exception {
        String ip = "225.80.248.37";
        String expectedUrl = "https://api.geoapify.com/v1/ipinfo?ip=225.80.248.37&apiKey=notAnApiKey";
        String jsonResponse = "{\"country\":{\"iso_code\":\"NZ\",\"name\":\"New Zealand\"},\"location\":{\"latitude\":-43.5234,\"longitude\":172.599}}";
        @SuppressWarnings("unchecked")
        ResponseEntity<String> mockResponse = (ResponseEntity<String>) mock(ResponseEntity.class);
        when(restTemplate.getForEntity(expectedUrl, String.class)).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.getBody()).thenReturn(jsonResponse);
        mockMvc.perform(MockMvcRequestBuilders.get("/localisation")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-Forwarded-For", ip)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country.iso_code").value("NZ"))
                .andExpect(jsonPath("$.country.name").value("New Zealand"))
                .andExpect(jsonPath("$.location.latitude").value(-43.5234))
                .andExpect(jsonPath("$.location.longitude").value(172.599));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testGetLocalisation_local_getsAllInformation() throws Exception {
        String ip = "127.0.0.1"; // localhost
        String expectedUrl = "https://api.geoapify.com/v1/ipinfo?&apiKey=notAnApiKey";
        String jsonResponse = "{\"country\":{\"iso_code\":\"NZ\",\"name\":\"New Zealand\"},\"location\":{\"latitude\":-43.5234,\"longitude\":172.599}}";
        @SuppressWarnings("unchecked")
        ResponseEntity<String> mockResponse = (ResponseEntity<String>) mock(ResponseEntity.class);
        when(restTemplate.getForEntity(expectedUrl, String.class)).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.getBody()).thenReturn(jsonResponse);
        mockMvc.perform(MockMvcRequestBuilders.get("/localisation")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .header("X-Forwarded-For", ip)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country.iso_code").value("NZ"))
                .andExpect(jsonPath("$.country.name").value("New Zealand"))
                .andExpect(jsonPath("$.location.latitude").value(-43.5234))
                .andExpect(jsonPath("$.location.longitude").value(172.599));
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void testGetAddressAttribution_notLocal_getsAllInformation() throws Exception {
        String expectedUrl = "https://api.geoapify.com/v1/geocode/autocomplete?text=10 Downing Street&filter=countrycode:UK&bias=proximity:51.4934,0.0000&type=street&lang=en&format=json&apiKey=notAnApiKey";
        String json = "[{\"formatted\": \"10 Downing Street, SW1A 2AA, London, United Kingdom\"}]";
        @SuppressWarnings("unchecked")
        ResponseEntity<String> mockResponse = (ResponseEntity<String>) mock(ResponseEntity.class);
        when(restTemplate.getForEntity(anyString(), String.class)).thenReturn(mockResponse); //todo replace any
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.getBody()).thenReturn(json);
        mockMvc.perform(MockMvcRequestBuilders.get("/address-autocomplete/{prompt}", "10 Downing Street")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("country.iso_code", "UK")
                        .param("country.name", "United Kingdom")
                        .param("location.latitude", "51.4934")
                        .param("location.longitude", "0.0000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].formatted").value("10 Downing Street, SW1A 2AA, London, United Kingdom"));
    }
}

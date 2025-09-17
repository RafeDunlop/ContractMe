package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import jakarta.annotation.PostConstruct;
import nz.ac.canterbury.seng302.homehelper.config.Keys;
import nz.ac.canterbury.seng302.homehelper.controller.LocationController;
import nz.ac.canterbury.seng302.homehelper.dto.CoordsDTO;
import nz.ac.canterbury.seng302.homehelper.dto.CountryDTO;
import nz.ac.canterbury.seng302.homehelper.dto.LocalisationDTO;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class LocationControllerIntegrationTest {

    private MockMvc mockMvc;

    private ObjectWriter writer;

    @Autowired
    private LocationController locationController;

    @Autowired
    private Keys keys;

    @SpyBean
    private LocationService locationService;

    @Mock
    private RestTemplate restTemplate;

    @PostConstruct
    private void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
        ReflectionTestUtils.setField(locationService, "restTemplate", restTemplate);
    }

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(locationService, "restTemplate", restTemplate);
        keys.setGeoapify("notAnApiKey");
        writer = (new ObjectMapper()).writer();
    }

    @Test
    public void testGetLocalisation_notLocal_getsAllInformation() throws Exception {
        String ip = "225.80.248.37";
        String expectedUrl = "https://api.geoapify.com/v1/ipinfo?ip=225.80.248.37&apiKey=notAnApiKey";
        String jsonResponse = "{\"country\":{\"iso_code\":\"NZ\",\"name\":\"New Zealand\"},\"location\":{\"latitude\":-43.5234,\"longitude\":172.599}}";
        @SuppressWarnings("unchecked")
        ResponseEntity<String> mockResponse = (ResponseEntity<String>) mock(ResponseEntity.class);
        when(restTemplate.getForEntity(expectedUrl, String.class)).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.getBody()).thenReturn(jsonResponse);
        mockMvc.perform(MockMvcRequestBuilders.get("/location/localisation")
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
    public void testGetLocalisation_local_getsAllInformation() throws Exception {
        String ip = "127.0.0.1"; // localhost
        String expectedUrl = "https://api.geoapify.com/v1/ipinfo?&apiKey=notAnApiKey";
        String jsonResponse = "{\"country\":{\"iso_code\":\"NZ\",\"name\":\"New Zealand\"},\"location\":{\"latitude\":-43.5234,\"longitude\":172.599}}";
        @SuppressWarnings("unchecked")
        ResponseEntity<String> mockResponse = (ResponseEntity<String>) mock(ResponseEntity.class);
        when(restTemplate.getForEntity(expectedUrl, String.class)).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.getBody()).thenReturn(jsonResponse);
        mockMvc.perform(MockMvcRequestBuilders.get("/location/localisation")
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
    public void testGetAddress_notLocal_getsAllInformation() throws Exception {
        String expectedUrl = "https://api.geoapify.com/v1/geocode/autocomplete?text=10+Downing+Street&filter=countrycode:uk&bias=proximity:51.493400,0.000000&lang=en&format=json&apiKey=notAnApiKey";
        String json = "{\"results\":[{\"formatted\": \"10 Downing Street, SW1A 2AA, London, United Kingdom\"}]}";
        @SuppressWarnings("unchecked")
        ResponseEntity<String> mockResponse = (ResponseEntity<String>) mock(ResponseEntity.class);
        LocalisationDTO localisationDTO = new LocalisationDTO();
        CountryDTO countryDTO = new CountryDTO();
        localisationDTO.setCountry(countryDTO);
        CoordsDTO coordsDTO = new CoordsDTO();
        localisationDTO.setLocation(coordsDTO);
        countryDTO.setIso_code("UK");
        countryDTO.setName("United Kingdom");
        coordsDTO.setLatitude(51.4934);
        coordsDTO.setLongitude(0f);
        when(restTemplate.getForEntity(expectedUrl, String.class)).thenReturn(mockResponse); 
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockResponse.getBody()).thenReturn(json);
        mockMvc.perform(MockMvcRequestBuilders.post("/location/address-autocomplete/{prompt}", "10 Downing Street")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(writer.writeValueAsString(localisationDTO))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].formatted").value("10 Downing Street, SW1A 2AA, London, United Kingdom"));
    }


}

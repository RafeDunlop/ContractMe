package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
@ActiveProfiles("test")
class RenovationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private RenovationTaskRepository renovationTaskRepository;

    private User currentUser;
    private User owner;

    @BeforeEach
    void setupUser() {
        currentUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(currentUser);

        owner = new User("Owner", "User", "owner@doe.com", "Password");
        owner.grantAuthority("ROLE_USER");
        userRepository.save(owner);

        User notOwner = new User("Not", "Owner", "not.owner@doe.com", "Password");
        notOwner.grantAuthority("ROLE_USER");
        userRepository.save(notOwner);

        RenovationRecord renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        renovationRecordRepository.save(renovationRecord);
    }

    /**
     * Tests posting to the create renovations page which will create a new renovation record under the current user. If all the details
     * (name, description, rooms) are in the correct format and then posted, the user is taken to the view page for that renovation and the
     * record is added to the repository.
     * @throws Exception if the request processing fails
     */
    @Test
    void postCreateRecord_validRecordDetails_createRecord() throws Exception {
        Page<RenovationRecord> userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertTrue(userRecords.getContent().isEmpty());

        // The new record has a name with diacritic letters and a description of length 512 to test regex and boundaries.
        mockMvc.perform(post("/renovations/create")
                        .param("name", "Rénövatiôn Onē")
                        .param("description", "A".repeat(512))
                        .param("roomList", "Room 1", "Room 2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/renovations/view?id=*"))
                .andExpect(flash().attribute("renovation",
                        hasProperty("name", is("Rénövatiôn Onē"))));

        userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertTrue(userRecords.getContent().isEmpty());
    }

    /**
     * Tests posting to the create renovations page with an invalid name. If the name input is not the correct format (regex accepts any letter,
     * number, hyphen, comma, and/or space) and then posted, the user stays on the "create record" page and the renovation is not added.
     * @throws Exception if the request processing fails
     */
    @Test
    void postCreateRecord_invalidNameInput_stayOnForm() throws Exception {
        // Regex rejects the exclamation mark.
        mockMvc.perform(post("/renovations/create")
                        .param("name", "Fail!")
                        .param("description", "")
                        .param("roomList", "Room", "Room")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/create"))
                .andExpect(flash().attribute("name", "Fail!"))
                .andExpect(flash().attribute("description", ""))
                .andExpect(flash().attribute("roomList", hasSize(2)));
    }

    /**
     * Tests posting to the create renovations page with an invalid description. If the description input is not the correct format (accepts length <=
     * 512 characters) and then posted, the user stays on the "create record" page and the renovation is not added.
     * @throws Exception if the request processing fails
     */
    @Test
    void postCreateRecord_invalidDescriptionInput_stayOnForm() throws Exception {
        // Description has a maximum length of 512.
        mockMvc.perform(post("/renovations/create")
                        .param("name", "Renovation One")
                        .param("description", "a".repeat(513))
                        .param("roomList", "Room", "Room")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/create"))
                .andExpect(flash().attribute("name", "Renovation One"))
                .andExpect(flash().attribute("description", "a".repeat(513)))
                .andExpect(flash().attribute("roomList", hasSize(2)));
    }

    /**
     * Tests posting to the create renovations page when the record name already exists. If a record is added and then a record with
     * the same name is posted to the user, the user stays on the "create record" page and the renovation is not added.
     * @throws Exception if the request processing fails
     */
    @Test
    void postCreateRecord_recordNameExists_stayOnForm() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        mockMvc.perform(post("/renovations/create")
                        .param("name", "Renovation One")
                        .param("description", "")
                        .param("roomList", "Room", "Room")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/create"))
                .andExpect(flash().attribute("name", "Renovation One"))
                .andExpect(flash().attribute("description", ""))
                .andExpect(flash().attribute("roomList", hasSize(2)));
    }

    /**
     * Tests deleting a renovation when the id in the link is associated with a current record and the record has no tasks.
     * A no-content response is then returned to show the user the deletion was successful.
     * @throws Exception if the request processing fails
     */
    @Test
    void deleteRecord_validRecordIdWithoutTask_deletionSuccess() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        Page<RenovationRecord> userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertFalse(userRecords.getContent().isEmpty());

        mockMvc.perform(delete("/renovations/delete/{id}", existingRecord.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertTrue(userRecords.getContent().isEmpty());
    }

    /**
     * Tests deleting a renovation when the id in the link is associated with a current record and the record has a task.
     * A no-content response is then returned to show the user the deletion was successful.
     * @throws Exception if the request processing fails
     */
    @Test
    void deleteRecord_validRecordIdWithTask_deletionSuccess() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        RenovationTask existingTask = new RenovationTask("Task", "description", List.of(), LocalDate.now(), existingRecord);
        renovationTaskRepository.save(existingTask);

        Page<RenovationRecord> userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertFalse(userRecords.getContent().isEmpty());

        mockMvc.perform(delete("/renovations/delete/{id}", existingRecord.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertTrue(userRecords.getContent().isEmpty());
    }

    /**
     * Tests deleting a renovation when the id is not inputted. Returns a not found error to let the
     * user know they there has to be an id inputted when deleting.
     * @throws Exception if the request processing fails
     */
    @Test
    void deleteRecord_nullRecordId_notFoundError() throws Exception {
        mockMvc.perform(delete("/renovations/delete/")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests deleting a renovation when the id in the link is associated with a current record, but a user who didn't
     * create the record tries deleting it. Returns a forbidden error to let the user know they can't delete records
     * not associated with their account.
     * @throws Exception if the request processing fails
     */
    @Test
    void deleteRecord_invalidUserForDelete_forbiddenError() throws Exception {
        User anotherUser = new User("John", "Doe", "john@doe.com", "password");
        userRepository.save(anotherUser);

        RenovationRecord existingRecord = new RenovationRecord(anotherUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        Page<RenovationRecord> userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertTrue(userRecords.getContent().isEmpty());

        mockMvc.perform(delete("/renovations/delete/{id}", existingRecord.getId())
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateSameRenovationNameForDifferentUsers() throws Exception {
        // First request for Jane
        mockMvc.perform(post("/renovations/create")
                        .param("name", "Test Renovation")
                        .param("description", "Test description by Jane")
                        .param("roomList", "Room A")
                        .with(user("jane@doe.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/renovations/view?id=*"));

        // Second request for NotOwner
        mockMvc.perform(post("/renovations/create")
                        .param("name", "Test Renovation")
                        .param("description", "Test description by NotOwner")
                        .param("roomList", "Room B")
                        .with(user("not.owner@doe.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/renovations/view?id=*"));

        // Check the renovation records
        List<RenovationRecord> renovationRecords = renovationRecordRepository.findAll();
        assertNotNull(renovationRecords);

        RenovationRecord janeRecord = renovationRecords.stream()
                .filter(record -> record.getUser().getEmail().equals("jane@doe.com"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Jane's renovation record not found"));

        RenovationRecord notOwnerRecord = renovationRecords.stream()
                .filter(record -> record.getUser().getEmail().equals("not.owner@doe.com"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("NotOwner's renovation record not found"));

        assertEquals("Test Renovation", janeRecord.getName());
        assertEquals("Test Renovation", notOwnerRecord.getName());
        assertNotEquals(janeRecord.getId(), notOwnerRecord.getId());
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    void getForm_renovationWithLocation_locationAdded() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "RenovationOneTag", "Room A Renovation", List.of("Room A"));
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1("16 Ingoldsby Street");
        addressDTO.setCountry("New Zealand");
        addressDTO.setPostcode("8023");
        addressDTO.setCity("Christchurch");
        addressDTO.setRegion("Sydenham");
        addressDTO.setLat(1D);
        addressDTO.setLon(1D);
        Location location = new Location(addressDTO.getAddress_line1(), addressDTO.getCountry(), addressDTO.getPostcode(), addressDTO.getCity(), addressDTO.getRegion(), addressDTO.getLat(), addressDTO.getLon());
        doReturn(location).when(locationService).locate(addressDTO);

        mockMvc.perform(post("/renovations/create")
                .param("name", testRecord.getName())
                .param("description", testRecord.getDescription())
                .param("roomList", "Kitchen", "Dining Room")
                .param("address_line1", addressDTO.getAddress_line1())
                .param("country", addressDTO.getCountry())
                .param("postcode", addressDTO.getPostcode())
                .param("city", addressDTO.getCity())
                .param("region", addressDTO.getRegion())
                .param("lat", Double.toString(addressDTO.getLat()))
                .param("lon", Double.toString(addressDTO.getLon()))
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        List<RenovationRecord> allRecords = renovationRecordRepository.findAll();
        assertFalse(allRecords.isEmpty(), "No renovation records saved");
        RenovationRecord saved = allRecords.get(1);
        Location loc = saved.getLocation();
        assertNotNull(loc, "Location should be set on renovation");
        assertEquals(addressDTO.getAddress_line1(), loc.getAddress());
        assertEquals(addressDTO.getCountry(), loc.getCountry());
        assertEquals(addressDTO.getCity(), loc.getCity());
        assertEquals(addressDTO.getRegion(), loc.getSuburb());
        assertEquals(addressDTO.getPostcode(), loc.getPostcode());
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    void editRenovation_validLocationDetails_LocationUpdated() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "RenovationOneTag", "Room A Renovation", List.of("Room A"));
        renovationRecordRepository.save(testRecord);

        String address = "33 Moorhouse Avenue";
        String country = "New Zealand";
        String postcode = "8011";
        String city = "Christchurch";
        String region = "Addington";
        Double lat = 1D;
        Double lon = 1D;
        Location stubLocation = new Location(address, country, postcode, city, region, lat, lon);
        doReturn(stubLocation).when(locationService).locate(any(AddressDTO.class));
        mockMvc.perform(post("/renovations/edit?id=" + testRecord.getId())
                        .param("address_line1", address)
                        .param("country", country)
                        .param("postcode", postcode)
                        .param("city", city)
                        .param("region", region)
                        .param("name", "Renovation")
                        .param("description", "Some words")
                        .param("roomList", "Room 1", "Room 2")
                        .param("lat", Double.toString(lat))
                        .param("lon", Double.toString(lon))
                        .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();

        Location location = testRecord.getLocation();
        assertNotNull(location, "Location should be set on renovation");
        assertEquals(address, location.getAddress());
        assertEquals(country, location.getCountry());
        assertEquals(city, location.getCity());
        assertEquals(region, location.getSuburb());
        assertEquals(postcode, location.getPostcode());
        assertEquals(lat, location.getLatitude());
        assertEquals(lon, location.getLongitude());
    }

    @Test
    @Transactional
    @WithMockUser(username = "jane@doe.com")
    void getForm_renovationWithoutLocation_locationNotAdded() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "RenovationOneTag", "Room A Renovation", List.of("Room A"));

        mockMvc.perform(post("/renovations/create")
                        .param("name", testRecord.getName())
                        .param("description", testRecord.getDescription())
                        .param("roomList", "Kitchen", "Dining Room")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        List<RenovationRecord> allRecords = renovationRecordRepository.findAll();
        assertFalse(allRecords.isEmpty(), "No renovation records saved");
        RenovationRecord saved = allRecords.get(1);
        Location loc = saved.getLocation();
        assertNotNull(loc, "Location should be set on renovation");
        assertNull(loc.getAddress());
        assertNull(null, loc.getCountry());
        assertNull(null, loc.getCity());
        assertNull(loc.getSuburb());
        assertNull(loc.getPostcode());
    }
}
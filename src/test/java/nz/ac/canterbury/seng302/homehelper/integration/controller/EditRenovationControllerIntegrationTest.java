package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
@ActiveProfiles("test")
public class EditRenovationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    private User currentUser;
    private User owner;
    private RenovationRecord renovationRecord;

    @BeforeEach
    public void setupUser() {
        currentUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(currentUser);

        owner = new User("Owner", "User", "owner@doe.com", "Password");
        owner.grantAuthority("ROLE_USER");
        userRepository.save(owner);

        User notOwner = new User("Not", "Owner", "not.owner@doe.com", "Password");
        notOwner.grantAuthority("ROLE_USER");
        userRepository.save(notOwner);

        renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        renovationRecordRepository.save(renovationRecord);
    }

    /**
     * Tests getting the edit renovation form with an id that is associated with an existing record. If the id is valid, the user is taken
     * to the edit page for that renovation with the input fields filled out with that renovation's details.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getEditRecord_validRecordId_returnForm() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        mockMvc.perform(get("/renovations/edit")
                        .param("id", Long.toString(existingRecord.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("editRenovationTemplate"))
                .andExpect(model().attribute("renovation", existingRecord));
    }

    /**
     * Tests getting the edit renovations form with an id not associated with any existing record. The application throws an exception,
     * and the user is not taken to the page.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getEditRecord_invalidRecordId_throwException() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        mockMvc.perform(get("/renovations/edit")
                        .param("id", Long.toString(existingRecord.getId() + 1))
                        .param("name", "Renovation One")
                        .param("description", "Some words")
                        .param("roomList", "Room 1", "Room 2")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("400 BAD_REQUEST \"This renovation does not exist\"", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void editRenovationRecord_userNotOwner_redirectToMain() throws Exception {
        mockMvc.perform(get("/renovations/edit")
                        .param("id", renovationRecord.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }

    /**
     * Tests posting to the edit renovations form which will update an existing renovation record. If all the updated details (name, description,
     * rooms) are in the correct format and then posted, the user is taken to the view page for that renovation and the record associated
     * with the id is updated in the repository.
     * @throws Exception if the request processing fails
     */
    @Test
    public void postEditRecord_validNewRecordDetails_updateRecord() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        // The updated record has a name with diacritic letters and a description of length 512 to test regex and boundaries.
        mockMvc.perform(post("/renovations/edit")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("name", "Rénövatiôn Onē")
                        .param("description", "A".repeat(512))
                        .param("taskRoomList", "Room 3", "Room 4")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + existingRecord.getId()));

        Page<RenovationRecord> userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertTrue(userRecords.getContent().isEmpty());

        userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Rénövatiôn Onē", null);
        assertFalse(userRecords.getContent().isEmpty());
    }

    /**
     * Tests posting to the edit renovations form with invalid details. If the name input is not the correct format (regex accepts any letter,
     * number, hyphen, comma, and/or space) and the description is too long, when the form is posted, the user stays on the same
     * page and the renovation is not updated.
     * @throws Exception if the request processing fails
     */
    @Test
    public void postEditRecord_invalidNewRecordDetails_stayOnForm() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        // Exclamation mark is rejected by regex, and the description has a maximum length of 512.
        mockMvc.perform(post("/renovations/edit")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("name", "Fail!")
                        .param("description", "A".repeat(513))
                        .param("roomList", "Room 1", "Room 2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/edit?id=" + existingRecord.getId()))
                .andExpect(flash().attribute("name", "Fail!"))
                .andExpect(flash().attribute("description", "A".repeat(513)))
                .andExpect(flash().attribute("roomList", List.of("Room 1", "Room 2")));


        Page<RenovationRecord> userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertFalse(userRecords.getContent().isEmpty());

        userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One!", null);
        assertTrue(userRecords.getContent().isEmpty());
    }

    /**
     * Tests posting to the edit renovations form when the record name already exists (not including the current renovation). If two records
     * with different names are added and then the second record is updated to have the same name as the first, the user stays on the same
     * page and the renovation is not updated.
     * @throws Exception if the request processing fails
     */
    @Test
    public void postEditRecord_recordNameExists_stayOnForm() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        RenovationRecord nameExistsRecord = new RenovationRecord(currentUser, "Renovation Two", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(nameExistsRecord);

        mockMvc.perform(post("/renovations/edit")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("name", "Renovation Two")
                        .param("description", "Some words")
                        .param("roomList", "Room 1", "Room 2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/edit?id=" + existingRecord.getId()))
                .andExpect(flash().attribute("name", "Renovation Two"))
                .andExpect(flash().attribute("description", "Some words"))
                .andExpect(flash().attribute("roomList", List.of("Room 1", "Room 2")));

        Page<RenovationRecord> userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertFalse(userRecords.getContent().isEmpty());
    }

    /**
     * Tests posting to the edit renovations form with an id not associated with any existing record. The application throws an exception,
     * and the renovation details are not updated.
     * @throws Exception if the request processing fails
     */
    @Test
    public void postEditRecord_invalidRecordId_throwException() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        mockMvc.perform(post("/renovations/edit")
                        .param("id", Long.toString(existingRecord.getId() + 1))
                        .param("name", "Renovation One")
                        .param("description", "Some words")
                        .param("roomList", "Room 1", "Room 2")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("400 BAD_REQUEST \"This renovation does not exist\"", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    public void testEditSameRenovationNameForDifferentUsers() throws Exception {
        mockMvc.perform(post("/renovations/create")
                        .param("name", "Test Renovation")
                        .param("description", "Test description by Jane")
                        .param("roomList", "Room A")
                        .with(user("jane@doe.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/renovations/view?id=*"));

        mockMvc.perform(post("/renovations/create")
                        .param("name", "Test Renovation")
                        .param("description", "Test description by NotOwner")
                        .param("roomList", "Room B")
                        .with(user("not.owner@doe.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/renovations/view?id=*"));

        List<RenovationRecord> renovationRecords = renovationRecordRepository.findAll();
        RenovationRecord janeRecord = renovationRecords.stream()
                .filter(record -> record.getUser().getEmail().equals("jane@doe.com"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Jane's renovation record not found"));

        RenovationRecord notOwnerRecord = renovationRecords.stream()
                .filter(record -> record.getUser().getEmail().equals("not.owner@doe.com"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("NotOwner's renovation record not found"));

        mockMvc.perform(post("/renovations/edit")
                        .param("id", String.valueOf(janeRecord.getId()))
                        .param("name", "Test Renovation")
                        .param("description", "Updated description by Jane")
                        .param("roomList", "Room A", "Room B")
                        .with(user("jane@doe.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/renovations/view?id=*"));

        mockMvc.perform(post("/renovations/edit")
                        .param("id", String.valueOf(notOwnerRecord.getId()))
                        .param("name", "Test Renovation")
                        .param("description", "Updated description by NotOwn 0er")
                        .param("roomList", "Room B", "Room C")
                        .with(user("not.owner@doe.com").roles("USER"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/renovations/view?id=*"));

        renovationRecords = renovationRecordRepository.findAll();
        assertNotNull(renovationRecords);

        janeRecord = renovationRecords.stream()
                .filter(record -> record.getUser().getEmail().equals("jane@doe.com"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Jane's renovation record not found"));

        notOwnerRecord = renovationRecords.stream()
                .filter(record -> record.getUser().getEmail().equals("not.owner@doe.com"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("NotOwner's renovation record not found"));

        assertEquals("Test Renovation", janeRecord.getName());
        assertEquals("Test Renovation", notOwnerRecord.getName());
        assertNotEquals(janeRecord.getId(), notOwnerRecord.getId());
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void editRenovation_invalidLocation_locationNotSaved() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "RenovationOneTag", "Room A Renovation", List.of("Room A"));
        renovationRecordRepository.save(testRecord);

        mockMvc.perform(post("/renovations/edit?id=" + testRecord.getId())
                        .param("address_line1", "1 Cool Street")
                        .param("country", "New  Zealand")
                        .param("postcode", "|}{)(*)&*&%")
                        .param("city", "Christ)(*)( church")
                        .param("region", "Foo$bar")
                        .param("name", "Renovation")
                        .param("description", "Some words")
                        .param("roomList", "Room 1", "Room 2")

                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("countryError", List.of("Country contains invalid characters.")))
                .andExpect(flash().attribute("cityError", List.of("City contains invalid characters.")))
                .andExpect(flash().attribute("postcodeError", List.of("Postcode contains invalid characters.")))
                .andExpect(flash().attribute("suburbError", List.of("Suburb contains invalid characters.")))
                .andReturn();

        RenovationRecord record = renovationRecordRepository.findById(testRecord.getId())
                .orElseThrow(() -> new AssertionError("Renovation record not found."));
        assertNull(record.getLocation());
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void editRenovation_existingLocationInvalidForm_locationNotUpdated() throws Exception {
        Location initialLocation = new Location(
                "10 Queen Street ", "Australia", "8011", "Sydney", "Mt Druit"
        );
        RenovationRecord testRecord = new RenovationRecord(owner, "Test Record", "Description", List.of("Room A"));
        testRecord.setLocation(initialLocation);
        renovationRecordRepository.save(testRecord);

        mockMvc.perform(post("/renovations/edit?id=" + testRecord.getId())
                        .param("address_line1", "33 Fendylton Ave")
                        .param("country", "New Zealand")
                        .param("postcode", "!!!!!!!!!")
                        .param("city", "Christchurch")
                        .param("region", "Fendylton")
                        .param("name", "Renovation")
                        .param("description", "Some words")
                        .param("roomList", "Room 1", "Room 2")

                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        renovationRecordRepository.findById(testRecord.getId()).orElseThrow(() -> new AssertionError("Optional null"));
        Location location = testRecord.getLocation();

        assertEquals(initialLocation.getAddress(), location.getAddress(), "Address should not change");
        assertEquals(initialLocation.getCity(), location.getCity(), "City should not change");
        assertEquals(initialLocation.getCountry(), location.getCountry(), "Country should not change");
        assertEquals(initialLocation.getPostcode(), location.getPostcode(), "Postcode should not change");
        assertEquals(initialLocation.getSuburb(), location.getSuburb(), "Region/suburb should not change");
    }

    /**
     * Test that the publicity flag of a renovation record is updated correctly.
     * <p>
     * This test creates a renovation record, sends a request to set its publicity flag to true, and checks that the
     * record is updated in the repository. It also verifies the correct redirection to the renovation details page.
     * </p>
     *
     * @throws Exception if an error occurs during the test execution
     */
    @Test
    public void changePublicFlag_setTrue_renovationIsPublic() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);
        Long id = existingRecord.getId();

        mockMvc.perform(post("/renovations/editPublicity/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"isPublic\": true}")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + id));

        RenovationRecord updated = renovationRecordRepository.findById(id).orElseThrow();
        assertTrue(updated.isPublic(), "Publicity flag should be updated to true");
    }

    /**
     * Test that the publicity flag of a renovation record is updated correctly.
     * <p>
     * This test creates a renovation record, sends a request to set its publicity flag to false, and checks that the
     * record is updated in the repository. It also verifies the correct redirection to the renovation details page.
     * </p>
     *
     * @throws Exception if an error occurs during the test execution
     */
    @Test
    public void changePublicFlag_setFalse_renovationIsNotPublic() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);
        Long id = existingRecord.getId();

        mockMvc.perform(post("/renovations/editPublicity/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"isPublic\": false}")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + id));

        RenovationRecord updated = renovationRecordRepository.findById(id).orElseThrow();
        assertFalse(updated.isPublic(), "Publicity flag should be updated to true");
    }
}

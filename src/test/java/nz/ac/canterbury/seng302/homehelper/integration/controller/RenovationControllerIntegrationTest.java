package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.CalendarCellDTO;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
@ActiveProfiles("test")
public class RenovationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private RenovationTaskRepository renovationTaskRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private RenovationRecordService renovationRecordService;

    @SpyBean
    private LocationService locationService;

    private User currentUser;
    private User owner;
    private User notOwner;
    private User testUser;

    private RenovationRecord renovationRecord;
    private MockHttpSession session;

    @BeforeEach
    public void setupUser() {

        currentUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(currentUser);

        owner = new User("Owner", "User", "owner@doe.com", "Password");
        owner.grantAuthority("ROLE_USER");
        userRepository.save(owner);

        notOwner = new User("Not", "Owner", "not.owner@doe.com", "Password");
        notOwner.grantAuthority("ROLE_USER");
        userRepository.save(notOwner);

        testUser = new User("Test", "User", "test@doe.com", "Password");
        testUser.grantAuthority("ROLE_USER");
        userRepository.save(testUser);

        renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        renovationRecordRepository.save(renovationRecord);

        session = new MockHttpSession();
    }

    /**
     * Tests the renovation page when the user has some renovation records. It only shows that user's records.
     * Simulates a user who has used the application before going to their renovation's page.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getRenovationForm_withUserWithoutUser_returnUserRecord() throws Exception {
        User anotherUser = new User("John", "Doe", "john@doe.com", "password");
        userRepository.save(anotherUser);

        renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation 1", "Some words", List.of("Room 1", "Room 2")));
        renovationRecordRepository.save(new RenovationRecord(anotherUser, "Renovation 2", "Some words", List.of("Room 3", "Room 4")));

        MvcResult result = mockMvc.perform(get("/renovations/retrieve"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        JsonNode root = new ObjectMapper().readTree(json);
        JsonNode records = root.path("content");

        assertTrue(StreamSupport.stream(records.spliterator(), false)
                .anyMatch(n -> n.get("name").asText().equals("Renovation 1")));

        assertFalse(StreamSupport.stream(records.spliterator(), false)
                .anyMatch(n -> n.get("name").asText().equals("Renovation 2")));
    }

    /**
     * Tests the renovation page when the user doesn't have any renovation records. Doesn't show any
     * records and has a message saying that no records have been made.
     * Simulates a first time user going to their renovation's page.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getRenovationRecord_noUserRecords_showNoRecordExist() throws Exception {
        MvcResult result = mockMvc.perform(get("/renovations/retrieve"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        JsonNode root = new ObjectMapper().readTree(json);
        assertTrue(root.get("content").isEmpty());
    }

    /**
     * Tests the name search function when there are records that match with the input. Only shows the records with names that
     * are similar to the inputted search string.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getRenovationForm_searchByName_returnMatchingRecord() throws Exception {
        renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2")));
        renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation Two", "Some words", List.of("Room 1", "Room 2")));
        renovationRecordRepository.save(new RenovationRecord(currentUser, "Tone", "Some words", List.of("Room 1", "Room 2")));

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("searchTerm", "One"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");
        List<String> names = new ArrayList<>();
        content.forEach(n -> names.add(n.get("name").asText()));

        assertTrue(names.contains("Renovation One"));
        assertTrue(names.contains("Tone"));
        assertFalse(names.contains("Renovation Two"));
    }

    /**
     * Tests the name search function when there aren't any records that match with the input. Doesn't show any records and has a
     * message saying that no records match the input.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getRenovationRecord_searchByName_showNoRecordWithName() throws Exception {
        renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2")));

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("searchTerm", "Two"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");
        assertTrue(content.isEmpty());
    }

    /**
     * Tests that a paginated page can be selected using query parametrs.
     * Verifies that the correct page is returned by specifiying the number
     * of records per page.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getRenovationRecord_selectPage_returnsCorrectPage() throws Exception {
        for (int i = 0; i < 20; i++) {
            renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1")));
        }

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("page", "2")
                        .param("cardsPerPage", "5"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        JsonNode content = root.get("content");
        List<String> names = new ArrayList<>();
        content.forEach(n -> names.add(n.get("name").asText()));

        assertEquals(4, root.get("totalPages").asInt());
        assertEquals(1, root.get("number").asInt());
        assertTrue(names.contains("Renovation 14"));
    }

    /**
     * Tests that selecting a page that is out of bounds will redirect to the last page.
     */
    @Test
    public void getRenovationRecord_selectOutOfBoundsPage_returnsLastPage() throws Exception {
        for (int i = 0; i < 20; i++) {
            renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1")));
        }

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("page", "100")
                        .param("cardsPerPage", "5"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        assertEquals(4, root.get("totalPages").asInt());
        assertEquals(3, root.get("number").asInt());
    }

    /**
     * Tests that selecting a page that is out of bounds will redirect to the last page.
     */
    @Test
    public void getRenovationRecord_selectNegativePage_returnsFirstPage() throws Exception {
        for (int i = 0; i < 20; i++) {
            renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1")));
        }

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("page", "-100")
                        .param("cardsPerPage", "5"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        assertEquals(4, root.get("totalPages").asInt());
        assertEquals(0, root.get("number").asInt());
    }

    /**
     * Tests that requesting 0 items per page will redirect to the default of 8 items per page.
     */
    @Test
    public void getRenovationRecord_zeroCardsPerPage_returns16ItemsPerPage() throws Exception {
        for (int i = 0; i < 20; i++) {
            RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1", "Room 2"));
            renovationRecordRepository.save(existingRecord);
        }

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("page", "1")
                        .param("cardsPerPage", "0"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        assertEquals(20, root.get("totalPages").asInt());
        assertEquals(0, root.get("number").asInt());
    }

    /**
     * Tests that requesting a page number of 0 will redirect to the first page.
     */
    @Test
    public void getRenovationRecord_zeroPageNumber_returnsFirstPage() throws Exception {
        for (int i = 0; i < 20; i++) {
            renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1")));
        }

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("page", "0")
                        .param("cardsPerPage", "5"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        assertEquals(4, root.get("totalPages").asInt());
        assertEquals(0, root.get("number").asInt());
    }

        /**
         * Tests posting to the create renovations page which will create a new renovation record under the current user. If all the details
         * (name, description, rooms) are in the correct format and then posted, the user is taken to the view page for that renovation and the
         * record is added to the repository.
         * @throws Exception if the request processing fails
         */
    @Test
    public void postCreateRecord_validRecordDetails_createRecord() throws Exception {
        Page<RenovationRecord> userRecords = renovationRecordRepository.findUserRecordsBySearch(currentUser, "Renovation One", null);
        assertTrue(userRecords.getContent().isEmpty());

        // New record has a name with diacritic letters and a description of length 512 to test regex and boundaries.
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
     * number, hyphen, comma, and/or space) and then posted, the user stays on the create record page and the renovation is not added.
     * @throws Exception if the request processing fails
     */
    @Test
    public void postCreateRecord_invalidNameInput_stayOnForm() throws Exception {
        // Exclamation mark is rejected by regex.
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
     * 512 characters) and then posted, the user stays on the create record page and the renovation is not added.
     * @throws Exception if the request processing fails
     */
    @Test
    public void postCreateRecord_invalidDescriptionInput_stayOnForm() throws Exception {
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
     * the same name is posted to the user, the user stays on the create record page and the renovation is not added.
     * @throws Exception if the request processing fails
     */
    @Test
    public void postCreateRecord_recordNameExists_stayOnForm() throws Exception {
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
     * A no content response is then returned to show the user the deletion was successful.
     * @throws Exception if the request processing fails
     */
    @Test
    public void deleteRecord_validRecordIdWithoutTask_deletionSuccess() throws Exception {
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
     * A no content response is then returned to show the user the deletion was successful.
     * @throws Exception if the request processing fails
     */
    @Test
    public void deleteRecord_validRecordIdWithTask_deletionSuccess() throws Exception {
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
    public void deleteRecord_nullRecordId_notFoundError() throws Exception {
        mockMvc.perform(delete("/renovations/delete/")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    /**
     * Tests deleting a renovation when the id in the link is associated with a current record but a user who didn't
     * create the record tries deleting it. Returns a forbidden error to let the user know they can't delete records
     * not associated with their account.
     * @throws Exception if the request processing fails
     */
    @Test
    public void deleteRecord_invalidUserForDelete_forbiddenError() throws Exception {
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
     * Tests getting the edit renovations form with an id not associated with any existing record. The application throws an exception
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

        // Updated record has a name with diacritic letters and a description of length 512 to test regex and boundaries.
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

        // Exclamation mark is rejected by regex and description has a maximum length of 512.
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
     * Tests posting to the edit renovations form with an id not associated with any existing record. The application throws an exception
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

    /**
     * Tests getting the view record page with an id associated with an existing record. The user is taken to the view records page for
     * that record where the renovation's details are shown.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getViewRecord_validRecordId_returnForm() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andExpect(model().attribute("renovation", existingRecord));
    }

    /**
     * Tests getting the view record page with an id not associated with any existing record. The application throws an exception
     * and the user is not taken to the page.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getViewRecord_invalidRecordId_throwException() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId() + 1))
                        .with(csrf()))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("404 NOT_FOUND \"This renovation does not exist\"", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    public void getViewRecord_withPagination_returnPaginatedTasks_JSON() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        List<RenovationTask> renovationTasks = IntStream.range(0, 15)
                .mapToObj(i -> new RenovationTask(
                        "Task " + i,
                        "Description for Task " + i,
                        List.of("Room 1", "Room 2"),
                        LocalDate.now().plusDays(i),
                        existingRecord
                ))
                .toList();
        existingRecord.setRenovationTasks(renovationTasks);
        renovationRecordRepository.save(existingRecord);

        ObjectMapper mapper = new ObjectMapper();

        // Page 1
        MvcResult result1 = mockMvc.perform(get("/renovations/retrieve/" + existingRecord.getId())
                        .param("page", "1")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root1 = mapper.readTree(result1.getResponse().getContentAsString());
        assertEquals(5, root1.get("content").size());
        assertEquals(0, root1.get("number").asInt());
        assertEquals(3, root1.get("totalPages").asInt());

        // Page 2
        MvcResult result2 = mockMvc.perform(get("/renovations/retrieve/" + existingRecord.getId())
                        .param("page", "2")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root2 = mapper.readTree(result2.getResponse().getContentAsString());
        assertEquals(5, root2.get("content").size());
        assertEquals(1, root2.get("number").asInt());
        assertEquals(3, root2.get("totalPages").asInt());

        // Page 3
        MvcResult result3 = mockMvc.perform(get("/renovations/retrieve/" + existingRecord.getId())
                        .param("page", "3")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root3 = mapper.readTree(result3.getResponse().getContentAsString());
        assertEquals(5, root3.get("content").size());
        assertEquals(2, root3.get("number").asInt());
        assertEquals(3, root3.get("totalPages").asInt());
    }

    @Test
    public void getRenovationTaskPages_withInvalidPageNumber_returnsDefaultOrLastPage() throws Exception {
        RenovationRecord record = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        List<RenovationTask> tasks = IntStream.range(0, 10)
                .mapToObj(i -> new RenovationTask(
                        "Task " + i,
                        "Description " + i,
                        List.of("Room 1", "Room 2"),
                        LocalDate.now().plusDays(i),
                        record
                ))
                .toList();
        record.setRenovationTasks(tasks);
        renovationRecordRepository.save(record);

        long recordId = record.getId();

        MvcResult outOfBoundsResult = mockMvc.perform(get("/renovations/retrieve/" + recordId)
                        .param("page", "34")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = new ObjectMapper().readTree(outOfBoundsResult.getResponse().getContentAsString());
        assertEquals(2, root.get("totalPages").asInt());
        assertEquals(1, root.get("number").asInt());

        MvcResult negativePageResult = mockMvc.perform(get("/renovations/retrieve/" + recordId)
                        .param("page", "-1")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode negativeRoot = new ObjectMapper().readTree(negativePageResult.getResponse().getContentAsString());
        assertEquals(0, negativeRoot.get("number").asInt()); // page 1 (0-based)
    }


    @Test
    @WithMockUser(username = "not.owner@example.com")
    // GitHub copilot generated some parts of the following test
    public void getViewRecord_notOwner_notFound() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "A cool renovation", "Some words", List.of("Room foo", "Room bar"));
        existingRecord = renovationRecordRepository.save(existingRecord);
        User loggedInUser = new User("Not", "Owner", "not.owner@example.com", "password");
        userRepository.save(loggedInUser);

        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void editRenovationRecord_userNotOwner_redirectToMain() throws Exception {
        mockMvc.perform(get("/renovations/edit")
                        .param("id", renovationRecord.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }

    @Test
    public void testCreateSameRenovationNameForDifferentUsers() throws Exception {
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
    public void testAutocompleteTags() throws Exception {
        tagService.createTag("historic");
        tagService.createTag("history");

        mockMvc.perform(get("/renovations/tags/autocomplete")
                .param("partialTag", "his"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("historic", "history")));

        tagService.createTag("building-one");

        mockMvc.perform(get("/renovations/tags/autocomplete")
                        .param("partialTag", "build"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItems("building-one")));
    }

    @Test
    public void testEmptyAutocompleteTags() throws Exception {
        mockMvc.perform(get("/renovations/tags/autocomplete")
                        .param("partialTag", "his"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        tagService.createTag("ancient");

        mockMvc.perform(get("/renovations/tags/autocomplete")
                        .param("partialTag", " "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testAddExistingTagToRenovation() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(currentUser, "Test Renovation", "Some words", List.of("Room1", "Room2"));
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();

        String testTagName = "apartment";
        tagService.createTag(testTagName);

        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", testTagName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + renovationId + "&page=1"));

        assertTrue(testRecord.getTags().stream()
                .anyMatch(tag -> tag.getTagName().equals(testTagName)));
    }

    @Test
    public void testAddNotExistingTagToRenovation() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(currentUser, "Test Renovation", "Some words", List.of("Room1", "Room2"));
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();

        String newTagName = "new-tag";
        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", newTagName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + renovationId  + "&page=1"));

        assertTrue(testRecord.getTags().stream()
                .anyMatch(tag -> tag.getTagName().equals(newTagName)));

        String newNameSpecialCharacters = "builder1!";
        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", newNameSpecialCharacters))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + renovationId + "&page=1"));

        assertTrue(testRecord.getTags().stream()
                .anyMatch(tag -> tag.getTagName().equals(newNameSpecialCharacters)));

        String withSpacesNewName = "      electrician";
        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", withSpacesNewName))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + renovationId + "&page=1"));

        assertTrue(testRecord.getTags().stream()
                .anyMatch(tag -> tag.getTagName().equals("electrician")));
    }

    @Test
    public void testAddTagInvalidInputs() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(currentUser, "Test Renovation", "Description", List.of());
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();

        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", "    "))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errors"));

        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", "123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errors"));
    }

    @Test
    public void addTagToRenovation_inappropriateTagName_profanityWarningThrown() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(currentUser, "Random Renovation", "Some words", List.of());
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();
        String tagName = "ass";

        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", tagName))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errors"))
                .andExpect(flash().attribute("errors", is(List.of("Name does not follow the system language standards."))));
    }

    @Test
    public void addTagToRenovation_noLettersAndAboveMaxLength_noLettersAndMaxLengthErrorThrown() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(currentUser, "Random Renovation", "Some words", List.of());
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();
        String tagName = "!".repeat(129);

        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", tagName))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errors"))
                .andExpect(flash().attribute("errors", containsInAnyOrder(
                        "Tags must contain one or more letters.",
                        "Tag cannot be greater than 128 characters.")));
    }

    @Test
    public void addTagToRenovation_recordIdDoesntExist_notFoundErrorThrown() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "Random Renovation", "Some words", List.of());
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();
        String tagName = "random tag 1";

        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId + 1))
                        .param("tagName", tagName))
                .andExpect(status().isNotFound());
    }

    @Test
    public void addTagToRenovation_recordNotOwnedByUser_unauthorizedErrorThrown() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "Random Renovation", "Some words", List.of());
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();
        String tagName = "random tag 2";

        mockMvc.perform(post("/renovations/tags/add")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", tagName))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void removeTagFromRenovation_validUserAndRecordId_tagDeletedAndNoContentResponse() throws Exception {
        Tag newTag = new Tag("random tag 3");
        tagRepository.save(newTag);
        RenovationRecord testRecord = new RenovationRecord(currentUser, "Test Renovation", "Some words", List.of("Room1", "Room2"));
        testRecord.addTag(newTag);
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();

        mockMvc.perform(patch("/renovations/tags/remove")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", newTag.getTagName()))
                .andExpect(status().isNoContent());

        assertTrue(testRecord.getTags().stream()
                .noneMatch(tag -> tag.getTagName().equals(newTag.getTagName())));
    }

    @Test
    public void removeTagFromRenovation_recordIdDoesntExist_notFoundErrorThrown() throws Exception {
        Tag newTag = new Tag("random tag 4");
        tagRepository.save(newTag);
        RenovationRecord testRecord = new RenovationRecord(currentUser, "Test Renovation", "Some words", List.of("Room1", "Room2"));
        testRecord.addTag(newTag);
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();

        mockMvc.perform(patch("/renovations/tags/remove")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId + 1))
                        .param("tagName", newTag.getTagName()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void removeTagFromRenovation_recordNotOwnedByUser_unauthorizedErrorThrown() throws Exception {
        Tag newTag = new Tag("random tag 5");
        tagRepository.save(newTag);
        RenovationRecord testRecord = new RenovationRecord(owner, "Test Renovation", "Some words", List.of("Room1", "Room2"));
        testRecord.addTag(newTag);
        renovationRecordRepository.save(testRecord);
        Long renovationId = testRecord.getId();

        mockMvc.perform(patch("/renovations/tags/remove")
                        .with(csrf())
                        .param("renovationId", String.valueOf(renovationId))
                        .param("tagName", newTag.getTagName()))
                .andExpect(status().isUnauthorized());

        assertFalse(testRecord.getTags().stream()
                .noneMatch(tag -> tag.getTagName().equals(newTag.getTagName())));
    }

    @Test
    public void searchRenovation_withNoMatches_returnsNoResults() throws Exception {
        String searchTerm = "NonExistentTerm";
        String visibility = "all";

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("searchTerm", searchTerm)
                        .param("visibility", visibility)
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = new ObjectMapper().readTree(result.getResponse().getContentAsString());
        assertTrue(root.get("content").isEmpty());
        assertEquals(0, root.get("totalPages").asInt());
        assertEquals(0, root.get("numberOfElements").asInt());
        assertEquals(16, root.get("size").asInt());
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void searchRenovation_withMatchingTerm_returnsMatchingRecords() throws Exception {
        RenovationRecord record = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        record.setPublicity(true);
        renovationRecordRepository.save(record);

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("searchTerm", "Test Renovation")
                        .param("visibility", "all")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");
        assertEquals(1, content.size());
        assertEquals("Test Renovation", content.get(0).get("name").asText());
    }


    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void searchRenovation_withVisibilityFilter_returnsFilteredResults() throws Exception {
        RenovationRecord publicRec = new RenovationRecord(owner, "Public Renovation", "Public Desc", List.of("Room A"));
        publicRec.setPublicity(true);
        RenovationRecord privateRec = new RenovationRecord(owner, "Private Renovation", "Private Desc", List.of("Room B"));
        privateRec.setPublicity(false);
        renovationRecordRepository.saveAll(List.of(publicRec, privateRec));

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("searchTerm", "")
                        .param("visibility", "public")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");

        assertEquals(1, content.size());
        assertEquals("Public Renovation", content.get(0).get("name").asText());
    }


    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void searchRenovation_withTermAndVisibilityFilter_returnsFilteredResults() throws Exception {
        RenovationRecord pub = new RenovationRecord(owner, "Public Renovation", "Room A", List.of("Room A"));
        pub.setPublicity(true);
        RenovationRecord priv = new RenovationRecord(owner, "Private Renovation", "Room B", List.of("Room B"));
        priv.setPublicity(false);
        renovationRecordRepository.saveAll(List.of(pub, priv));

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("searchTerm", "Public")
                        .param("visibility", "public")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");

        assertEquals(1, content.size());
        assertEquals("Public Renovation", content.get(0).get("name").asText());
    }

    @Test
    public void tagSearch_withValidPublicRenovation_displaysMoreMatchingTagsFirst() throws Exception {
        RenovationRecord oneTag = new RenovationRecord(owner, "RenovationOneTag", "A", List.of("Room A"));
        RenovationRecord twoTags = new RenovationRecord(owner, "RenovationTwoTags", "A", List.of("Room A"));
        oneTag.setPublicity(true);
        twoTags.setPublicity(true);

        Tag t1 = new Tag("House");
        Tag t2 = new Tag("New");
        tagRepository.saveAll(List.of(t1, t2));

        oneTag.getTags().add(t1);
        twoTags.getTags().addAll(List.of(t1, t2));
        renovationRecordRepository.saveAll(List.of(oneTag, twoTags));

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("tagNameList", "House")
                        .param("visibility", "all")
                        .param("tagNameList", "New")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode content = root.get("content");

        List<String> names = new ArrayList<>();
        for (JsonNode node : content) {
            names.add(node.get("name").asText());
        }

        assertEquals(List.of("RenovationTwoTags", "RenovationOneTag"), names);
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void getSearchRenovations_withSessionAttributes_rendersSearchPage() throws Exception {
        session.setAttribute("visibility", "user");
        session.setAttribute("searchTerm", "Test Renovation");

        mockMvc.perform(get("/renovations/search")
                        .param("visibility", "user")
                        .param("searchTerm", "Test Renovation")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attribute("visibility", is("user")))
                .andExpect(model().attribute("searchTerm", is("Test Renovation")));
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void getSearchRenovations_withNoMatchingRecords_returnsEmptyResults() throws Exception {
        session.setAttribute("visibility", "all");
        session.setAttribute("searchTerm", "NonExistent");

        ObjectMapper mapper = new ObjectMapper();

        MvcResult result1 = mockMvc.perform(get("/renovations/retrieve")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root1 = mapper.readTree(result1.getResponse().getContentAsString());
        assertEquals(0, root1.get("content").size());
        assertEquals(0, root1.get("number").asInt());
        assertEquals(0, root1.get("totalPages").asInt());
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void getSearchRenovations_withMatchingRecords_returnsFilteredResults() throws Exception {
        RenovationRecord record = new RenovationRecord(owner, "Renovation One", "Description", List.of("Room A"));
        record.setPublicity(true);
        renovationRecordRepository.save(record);

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("searchTerm", "One")
                        .param("visibility", "all"))

                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");
        List<String> names = new ArrayList<>();
        content.forEach(n -> names.add(n.get("name").asText()));

        assertTrue(names.contains("Renovation One"));
        assertEquals(1, content.size());
    }

    @Test
    public void tagSearch_withValidPublicRenovation_displaysListOfTags() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "Test Renovation 1", "Room A Renovation", List.of("Room A"));
        testRecord.setPublicity(true);
        RenovationRecord testRecord2 = new RenovationRecord(owner, "Test Renovation 2", "Room A Renovation", List.of("Room A"));
        String tagName ="House";
        Tag testTag = new Tag(tagName);
        tagRepository.save(testTag);
        testRecord.getTags().add(testTag);
        testRecord2.getTags().add(testTag);
        renovationRecordRepository.save(testRecord);
        renovationRecordRepository.save(testRecord2);

        mockMvc.perform(get("/renovations/search")
                        .session(session))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("visibility", "all")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");
        List<String> names = new ArrayList<>();
        content.forEach(n -> names.add(n.get("name").asText()));

        assertTrue(names.contains("Test Renovation 1"));
        assertFalse(names.contains("Test Renovation 2"));
        assertEquals(1, content.size());
    }

    @Test
    public void tagSearch_withNoPublicRenovation_NoResults() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "Test Renovation", "Room A Renovation", List.of("Room A"));
        String tagName ="Apartment";
        Tag testTag = new Tag(tagName);
        tagRepository.save(testTag);
        testRecord.getTags().add(testTag);
        renovationRecordRepository.save(testRecord);

        mockMvc.perform(get("/renovations/search")
                        .param("tagNameList", tagName)
                        .session(session))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("tagNameList", tagName)
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");

        assertEquals(0, content.size());
    }

    @Test
    public void tagSearch_withValidPublicRenovation_displaysMoreMatchingTagsFirstListOfTags() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "RenovationOneTag", "Room A Renovation", List.of("Room A"));
        RenovationRecord testRecord2 = new RenovationRecord(owner, "RenovationTwoTags", "Room A Renovation", List.of("Room A"));
        testRecord.setPublicity(true);
        testRecord2.setPublicity(true);

        String tagName ="House";
        String tagName2 ="New";
        Tag testTag = new Tag(tagName);
        Tag testTag2 = new Tag(tagName2);
        tagRepository.save(testTag);
        tagRepository.save(testTag2);

        testRecord.getTags().add(testTag);
        testRecord2.getTags().add(testTag);
        testRecord2.getTags().add(testTag2);
        renovationRecordRepository.save(testRecord);
        renovationRecordRepository.save(testRecord2);

        mockMvc.perform(get("/renovations/search")
                        .param("tagNameList", tagName)
                        .param("tagNameList", tagName2)
                        .session(session))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("tagNameList", tagName)
                        .param("tagNameList", tagName2)
                        .param("visibility", "all")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");
        List<String> names = new ArrayList<>();
        content.forEach(n -> names.add(n.get("name").asText()));

        assertTrue(names.contains("RenovationTwoTags"));
        assertTrue(names.contains("RenovationOneTag"));
    }


    @Test
    @WithMockUser(username = "jane@doe.com")
    public void getForm_renovationWithLocation_locationAdded() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "RenovationOneTag", "Room A Renovation", List.of("Room A"));
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1("164 Ingoldsby Street");
        addressDTO.setCountry("New Zealand");
        addressDTO.setPostcode("8023");
        addressDTO.setCity("Christchurch");
        addressDTO.setRegion("Beckenham");
        addressDTO.setLat(1D);
        addressDTO.setLon(1D);

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
    public void editRenovation_validLocationDetails_LocationUpdated() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "RenovationOneTag", "Room A Renovation", List.of("Room A"));
        renovationRecordRepository.save(testRecord);

        String address = "33 Moorhouse Ave";
        String country = "New Zealand";
        String postcode = "8043";
        String city = "Christchurch";
        String region = "Sydenham";
        Double lat = 1D;
        Double lon = 1D;

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

        RenovationRecord record = renovationRecordRepository.findById(testRecord.getId()).get();
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

        RenovationRecord record = renovationRecordRepository.findById(testRecord.getId())
                .orElseThrow(() -> new AssertionError("Optional null"));
        Location location = testRecord.getLocation();

        assertEquals(initialLocation.getAddress(), location.getAddress(), "Address should not change");
        assertEquals(initialLocation.getCity(), location.getCity(), "City should not change");
        assertEquals(initialLocation.getCountry(), location.getCountry(), "Country should not change");
        assertEquals(initialLocation.getPostcode(), location.getPostcode(), "Postcode should not change");
        assertEquals(initialLocation.getSuburb(), location.getSuburb(), "Region/suburb should not change");
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void getForm_renovationWithoutLocation_locationNotAdded() throws Exception {
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

    @Test
    public void getProfanityFilter_invalidName_returnTrue() throws Exception {
        String invalidName = "ass";
        mockMvc.perform(get("/renovations/tags/profanity-filter").param("tagName", invalidName))
                .andExpect(content().string(equalTo("true")))
                .andExpect(status().isOk());
    }

    @Test
    public void getProfanityFilter_validName_returnFalse() throws Exception {
        String invalidName = "Bathroom";
        mockMvc.perform(get("/renovations/tags/profanity-filter").param("tagName", invalidName))
                .andExpect(content().string(equalTo("false")))
                .andExpect(status().isOk());
    }

    @Test
    public void viewRenovation_validYearAndMonth_returnFormWithInputtedMonthAndYear() throws Exception {
        int inputtedYear = 2024;
        int inputtedMonth = 12;

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 1", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) Objects.requireNonNull(result.getModelAndView()).getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(inputtedYear, returnedDate.getYear());
        Assertions.assertEquals(inputtedMonth, returnedDate.getMonthValue());
        Assertions.assertEquals(6, returnedCalendarCells.size());
    }

    @Test
    public void viewRenovation_validMonthNoYear_returnFormWithInputtedMonthAndCurrentYear() throws Exception {
        int inputtedMonth = 12;
        int currentYear = LocalDate.now().getYear();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 2", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) result.getModelAndView().getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(currentYear, returnedDate.getYear());
        Assertions.assertEquals(inputtedMonth, returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void viewRenovation_noMonthAndYear_returnFormWithCurrentMonthAndYear() throws Exception {
        LocalDate now = LocalDate.now();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 3", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) result.getModelAndView().getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(now.getYear(), returnedDate.getYear());
        Assertions.assertEquals(now.getMonthValue(), returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void viewRenovation_negativeYear_returnFormWithMonthAndPositiveYear() throws Exception {
        int inputtedMonth = 12;
        int inputtedYear = 0;
        int expectedYear = LocalDate.now().getYear();
        int expectedMonth = LocalDate.now().getMonthValue();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 4", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) result.getModelAndView().getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(expectedYear, returnedDate.getYear());
        Assertions.assertEquals(expectedMonth, returnedDate.getMonthValue());
        Assertions.assertEquals(5, returnedCalendarCells.size());
    }

    @Test
    public void viewRenovation_invalidMonth_returnFormWithCurrentMonthAndYear() throws Exception {
        int inputtedMonth = 13;
        int inputtedYear = 2025;
        LocalDate now = LocalDate.now();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 5", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) result.getModelAndView().getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(now.getYear(), returnedDate.getYear());
        Assertions.assertEquals(now.getMonthValue(), returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void calendar_validYearAndMonth_returnFormWithInputtedMonthAndYear() throws Exception {
        int inputtedYear = 2024;
        int inputtedMonth = 12;

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 1", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) Objects.requireNonNull(result.getModelAndView()).getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(inputtedYear, returnedDate.getYear());
        Assertions.assertEquals(inputtedMonth, returnedDate.getMonthValue());
        Assertions.assertEquals(6, returnedCalendarCells.size());
    }

    @Test
    public void calendar_validMonthNoYear_returnFormWithInputtedMonthAndCurrentYear() throws Exception {
        int inputtedMonth = 12;
        int currentYear = LocalDate.now().getYear();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 2", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) result.getModelAndView().getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(currentYear, returnedDate.getYear());
        Assertions.assertEquals(inputtedMonth, returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void calendar_noMonthAndYear_returnFormWithCurrentMonthAndYear() throws Exception {
        LocalDate now = LocalDate.now();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 3", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) result.getModelAndView().getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(now.getYear(), returnedDate.getYear());
        Assertions.assertEquals(now.getMonthValue(), returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void calendar_negativeYear_returnFormWithMonthAndPositiveYear() throws Exception {
        int inputtedMonth = 12;
        int inputtedYear = 0;
        int expectedYear = LocalDate.now().getYear();
        int expectedMonth = LocalDate.now().getMonthValue();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 4", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) result.getModelAndView().getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(expectedYear, returnedDate.getYear());
        Assertions.assertEquals(expectedMonth, returnedDate.getMonthValue());
        Assertions.assertEquals(5, returnedCalendarCells.size());
    }

    @Test
    public void calendar_invalidMonth_returnFormWithCurrentMonthAndYear() throws Exception {
        int inputtedMonth = 13;
        int inputtedYear = 2025;
        LocalDate now = LocalDate.now();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 5", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        LocalDate returnedDate = (LocalDate) result.getModelAndView().getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(now.getYear(), returnedDate.getYear());
        Assertions.assertEquals(now.getMonthValue(), returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void calendar_dateEditedPresent_modelContainsDate() throws Exception {
        String dateToReturnTo = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("dateEdted", dateToReturnTo)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andExpect(model().attribute("renovation", existingRecord))
                .andReturn();

        Assertions.assertTrue(Objects.requireNonNull(result.getModelAndView()).getModelMap().containsKey("dateFormatter"));
    }
}

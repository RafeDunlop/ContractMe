package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.*;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
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

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation 1", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        RenovationRecord hiddenRecord = new RenovationRecord(anotherUser, "Renovation 2", "Some words", List.of("Room 3", "Room 4"));
        renovationRecordRepository.save(hiddenRecord);

        // Checks if the current user (jane@doe.com) can only see their records and none of the other users' ones.
        mockMvc.perform(get("/renovations"))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationsTemplate"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", hasItem(
                        hasProperty("name", is("Renovation 1")))))
                .andExpect(model().attribute("records", not(hasItem(
                        hasProperty("name", is("Renovation 2"))))));
    }

    /**
     * Tests the renovation page when the user doesn't have any renovation records. Doesn't show any
     * records and has a message saying that no records have been made.
     * Simulates a first time user going to their renovation's page.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getRenovationRecord_noUserRecords_showNoRecordExist() throws Exception {
        mockMvc.perform(get("/renovations"))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationsTemplate"))
                .andExpect(model().attributeExists("records"))
                .andExpect(content().string(containsString("No Renovations have been made yet.")))
                .andExpect(content().string(not(containsString("No Renovations found."))));

    }

    /**
     * Tests the name search function when there are records that match with the input. Only shows the records with names that
     * are similar to the inputted search string.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getRenovationForm_searchByName_returnMatchingRecord() throws Exception {
        RenovationRecord existingRecord1 = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord1);

        RenovationRecord existingRecord2 = new RenovationRecord(currentUser, "Renovation Two", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord2);

        RenovationRecord existingRecord3 = new RenovationRecord(currentUser, "Tone", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord3);

        String matchingName = "One";

        // Records that have names with the matching string anywhere within it should only be shown to the user.
        mockMvc.perform(get("/renovations")
                        .param("searchTerm", matchingName))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationsTemplate"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", hasItem(
                        hasProperty("name", is("Renovation One")))))
                .andExpect(model().attribute("records", hasItem(
                        hasProperty("name", is("Tone")))))
                .andExpect(model().attribute("records", not(hasItem(
                        hasProperty("name", is("Renovation Two"))))));
    }

    /**
     * Tests the name search function when there aren't any records that match with the input. Doesn't show any records and has a
     * message saying that no records match the input.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getRenovationRecord_searchByName_showNoRecordWithName() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        String matchingName = "Two";

        mockMvc.perform(get("/renovations")
                        .param("searchQuery", matchingName))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationsTemplate"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", not(hasItem(
                        hasProperty("name", is("Renovation One"))))))
                .andExpect(content().string(containsString("No renovations match your search.")))
                .andExpect(content().string(not(containsString("No Renovations have been made yet."))));
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
            RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1", "Room 2"));
            renovationRecordRepository.save(existingRecord);
        }
        mockMvc.perform(get("/renovations")
                        .param("page", "2")
                        .param("itemsPerPage", "5"))
            .andExpect(status().isOk())
            .andExpect(view().name("renovationsTemplate"))
            .andExpect(model().attributeExists("renovations"))
            .andExpect(model().attribute("records", hasSize(5)))
            .andExpect(model().attribute("pageNumber", 2))
            .andExpect(model().attribute("records", hasItem(hasProperty("name", is("Renovation 5")))));
    }

    /**
     * Tests that selecting a page that is out of bounds will redirect to the last page.
     */
    @Test
    public void getRenovationRecord_selectOutOfBoundsPage_returnsLastPage() throws Exception {
        for (int i = 0; i < 20; i++) {
            RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1", "Room 2"));
            renovationRecordRepository.save(existingRecord);
        }
        mockMvc.perform(get("/renovations")
                        .param("page", "100")
                        .param("itemsPerPage", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations?page=4&itemsPerPage=5"));
    }

    /**
     * Tests that requesting 0 items per page will redirect to the default of 8 items per page.
     */
//    @Test
//    public void getRenovationRecord_zeroItemsPerPage_returns8ItemsPerPage() throws Exception {
//        for (int i = 0; i < 20; i++) {
//            RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1", "Room 2"));
//            renovationRecordRepository.save(existingRecord);
//        }
//        mockMvc.perform(get("/renovations")
//                        .param("page", "1")
//                        .param("itemsPerPage", "0"))
//                .andExpect(status().is2xxSuccessful())
//                .andExpect(model().attribute("itemsPerPage", 8));
//    }

    /**
     * Tests that requesting a page number of 0 will redirect to the first page.
     */
    @Test
    public void getRenovationRecord_zeroPageNumber_returnsFirstPage() throws Exception {
        for (int i = 0; i < 20; i++) {
            RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1", "Room 2"));
            renovationRecordRepository.save(existingRecord);
        }
        mockMvc.perform(get("/renovations")
                        .param("page", "0")
                        .param("itemsPerPage", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations?page=1&itemsPerPage=5"));
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
                        .contentType(MediaType.APPLICATION_JSON)
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
                        .contentType(MediaType.APPLICATION_JSON)
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
    public void getViewRecord_withPagination_returnPaginatedTasks() throws Exception {
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

        // Test first page
        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("page", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andExpect(model().attribute("renovation", existingRecord))
                .andExpect(model().attribute("tasks", hasSize(5)))
                .andExpect(model().attribute("pageNumber", 1))
                .andExpect(model().attribute("totalPages", 3));

        // Test second page
        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("page", "2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andExpect(model().attribute("tasks", hasSize(5)))
                .andExpect(model().attribute("pageNumber", 2))
                .andExpect(model().attribute("totalPages", 3));

        // Test last page
        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("page", "3")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andExpect(model().attribute("tasks", hasSize(5)))
                .andExpect(model().attribute("pageNumber", 3))
                .andExpect(model().attribute("totalPages", 3));
    }

    @Test
    public void getViewRecord_invalidPageNumber_returnDefaultPage() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        List<RenovationTask> renovationTasks = IntStream.range(0, 10)
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

        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("page", "34")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + existingRecord.getId() + "&page=2"));


        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("page", "-1")
                        .param("cardPerPage", "5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + existingRecord.getId() + "&page=1"));
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
    public void searchRenovation_withNoMatches_returnsNoResultsMessage() throws Exception {
        String searchTerm = "NonExistentTerm";
        String visibility = "all";

        mockMvc.perform(get("/renovations/search")
                        .param("searchTerm", searchTerm)
                        .param("visibility", visibility)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", hasSize(0)))
                .andExpect(model().attribute("totalCards", is(0)))
                .andExpect(model().attribute("totalPages", is(0)))
                .andExpect(model().attribute("pageNumber", is(1)))
                .andExpect(model().attribute("cardsPerPage", is(16)))
                .andExpect(model().attribute("visibility", is(visibility)))
                .andExpect(model().attribute("searchTerm", is(searchTerm)));
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void searchRenovation_withMatchingTerm_returnsMatchingRecords() throws Exception {
        String searchTerm = "Test Renovation";
        String visibility = "all";


        RenovationRecord matchingRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        matchingRecord.setPublicity(true);
        renovationRecordRepository.save(matchingRecord);

        // Step 1: Perform the POST request to trigger the search
        MvcResult postResult = mockMvc.perform(post("/renovations/search")
                        .param("searchTerm", searchTerm)
                        .param("visibility", visibility)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        String redirectedUrl = postResult.getResponse().getRedirectedUrl();

        // Step 2: Follow GET redirect and assert results
        mockMvc.perform(get("/renovations/search")
                        .param("searchTerm", searchTerm)
                        .param("visibility", visibility)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", hasSize(1)))
                .andExpect(model().attribute("totalCards", is(1)))
                .andExpect(model().attribute("totalPages", is(1)))
                .andExpect(model().attribute("pageNumber", is(1)))
                .andExpect(model().attribute("cardsPerPage", is(16)))
                .andExpect(model().attribute("visibility", is(visibility)))
                .andExpect(model().attribute("searchTerm", is(searchTerm)))
                .andExpect(model().attribute("records", hasItem(
                        hasProperty("name", is(searchTerm))
                )));

    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void searchRenovation_withVisibilityFilter_returnsFilteredResults() throws Exception {
        RenovationRecord publicRecord = new RenovationRecord(owner, "Public Renovation", "Public Description", List.of("Room A"));
        publicRecord.setPublicity(true);
        renovationRecordRepository.save(publicRecord);

        RenovationRecord privateRecord = new RenovationRecord(owner, "Private Renovation", "Private Description", List.of("Room B"));
        privateRecord.setPublicity(false);
        renovationRecordRepository.save(privateRecord);

        mockMvc.perform(get("/renovations/search")
                        .param("searchTerm", "")
                        .param("visibility", "public")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate")) // ensure your GET method returns this view
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", hasSize(1)))
                .andExpect(model().attribute("records", hasItem(
                        hasProperty("name", is("Public Renovation")))))
                .andExpect(model().attribute("records", not(hasItem(
                        hasProperty("name", is("Private Renovation"))))));
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void searchRenovation_withTermAndVisibilityFilter_returnsFilteredResults() throws Exception {
        RenovationRecord publicRecord = new RenovationRecord(owner, "Public Renovation", "Room A Renovation", List.of("Room A"));
        publicRecord.setPublicity(true);
        renovationRecordRepository.save(publicRecord);

        RenovationRecord privateRecord = new RenovationRecord(owner, "Private Renovation", "Room B Renovation", List.of("Room B"));
        privateRecord.setPublicity(false);
        renovationRecordRepository.save(privateRecord);

        mockMvc.perform(get("/renovations/search")
                        .param("searchTerm", "Public")
                        .param("visibility", "public")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", hasSize(1)))
                .andExpect(model().attribute("records", hasItem(
                        hasProperty("name", is("Public Renovation")))))
                .andExpect(model().attribute("records", not(hasItem(
                        hasProperty("name", is("Private Renovation"))))));
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void getSearchRenovations_withDefaultValues_rendersSearchPageWithDefaultVisibility() throws Exception {
        mockMvc.perform(get("/renovations/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attribute("visibility", is("all")))
                .andExpect(model().attribute("searchTerm", is("")))
                .andExpect(model().attribute("records", hasSize(0)));
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

        mockMvc.perform(get("/renovations/search").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attribute("records", hasSize(0))); // No matching records
    }

    @Test
    @WithMockUser(username = "not.owner@doe.com")
    public void getSearchRenovations_withMatchingRecords_returnsFilteredResults() throws Exception {
        RenovationRecord record = new RenovationRecord(owner, "Test Renovation", "Description", List.of("Room A"));
        record.setPublicity(true);
        renovationRecordRepository.save(record);

        session.setAttribute("visibility", "all");
        session.setAttribute("searchTerm", "Test Renovation");

        mockMvc.perform(get("/renovations/search").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attribute("records", hasSize(1)))
                .andExpect(model().attribute("records", hasItem(
                        hasProperty("name", is("Test Renovation"))
                )));
    }

    @Test
    public void tagSearch_withValidPublicRenovation_displaysListOfTags() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "Test Renovation", "Room A Renovation", List.of("Room A"));
        testRecord.setPublicity(true);
        RenovationRecord testRecord2 = new RenovationRecord(owner, "Test Renovation", "Room A Renovation", List.of("Room A"));
        String tagName ="House";
        Tag testTag = new Tag(tagName);
        tagRepository.save(testTag);
        testRecord.getTags().add(testTag);
        testRecord2.getTags().add(testTag);
        renovationRecordRepository.save(testRecord);
        renovationRecordRepository.save(testRecord2);

        mockMvc.perform(get("/renovations/search")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", hasSize(1)))
                .andExpect(model().attribute("records", hasItem(
                        hasProperty("name", is("Test Renovation")))));
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
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attributeExists("records"))
                .andExpect(model().attribute("records", hasSize(0)));
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
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andExpect(model().attribute("records", hasSize(2)))
                .andExpect(model().attribute("records", contains(
                        hasProperty("name", is("RenovationTwoTags")),
                        hasProperty("name", is("RenovationOneTag"))
                )));
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

        mockMvc.perform(post("/renovations/create")
                .param("name", testRecord.getName())
                .param("description", testRecord.getDescription())
                .param("roomList", "Kitchen", "Dining Room")
                .param("address_line1", addressDTO.getAddress_line1())
                .param("country", addressDTO.getCountry())
                .param("postcode", addressDTO.getPostcode())
                .param("city", addressDTO.getCity())
                .param("region", addressDTO.getRegion())
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

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1("33 Moorhouse Ave");
        addressDTO.setCountry("New Zealand");
        addressDTO.setPostcode("8043");
        addressDTO.setCity("Christchurch");
        addressDTO.setRegion("Sydenham");

        mockMvc.perform(post("/renovations/edit?id=" + testRecord.getId())
                    .param("address_line1", addressDTO.getAddress_line1())
                    .param("country", addressDTO.getCountry())
                    .param("postcode", addressDTO.getPostcode())
                    .param("city", addressDTO.getCity())
                    .param("region", addressDTO.getRegion())
                        .param("name", "Renovation")
                        .param("description", "Some words")
                        .param("roomList", "Room 1", "Room 2")

                    .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andReturn();

        Location location = testRecord.getLocation();
        assertNotNull(location, "Location should be set on renovation");
        assertEquals(addressDTO.getAddress_line1(), location.getAddress());
        assertEquals(addressDTO.getCountry(), location.getCountry());
        assertEquals(addressDTO.getCity(), location.getCity());
        assertEquals(addressDTO.getRegion(), location.getSuburb());
        assertEquals(addressDTO.getPostcode(), location.getPostcode());
    }

    @Test
    @WithMockUser(username = "jane@doe.com")
    public void editRenovation_invalidLocation_locationNotSaved() throws Exception {
        RenovationRecord testRecord = new RenovationRecord(owner, "RenovationOneTag", "Room A Renovation", List.of("Room A"));
        renovationRecordRepository.save(testRecord);

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress_line1("1 Cool Street");
        addressDTO.setCountry("New  Zealand");
        addressDTO.setPostcode("|}{)(*)&*&%");
        addressDTO.setCity("Christ)(*)( church");
        addressDTO.setRegion("Foo$bar");

        mockMvc.perform(post("/renovations/edit?id=" + testRecord.getId())
                        .param("address_line1", addressDTO.getAddress_line1())
                        .param("country", addressDTO.getCountry())
                        .param("postcode", addressDTO.getPostcode())
                        .param("city", addressDTO.getCity())
                        .param("region", addressDTO.getRegion())
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
    public void getForm_renovationWitoutLocation_locationNotAdded() throws Exception {
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

}

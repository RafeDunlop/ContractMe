package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    private User currentUser;

    @BeforeEach
    public void setupUser() {
        currentUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(currentUser);
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
                .andExpect(model().attributeExists("renovations"))
                .andExpect(model().attribute("renovations", hasItem(
                        hasProperty("name", is("Renovation 1")))))
                .andExpect(model().attribute("renovations", not(hasItem(
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
                .andExpect(model().attributeExists("renovations"))
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
                        .param("searchQuery", matchingName))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationsTemplate"))
                .andExpect(model().attributeExists("renovations"))
                .andExpect(model().attribute("renovations", hasItem(
                        hasProperty("name", is("Renovation One")))))
                .andExpect(model().attribute("renovations", hasItem(
                        hasProperty("name", is("Tone")))))
                .andExpect(model().attribute("renovations", not(hasItem(
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
                .andExpect(model().attributeExists("renovations"))
                .andExpect(model().attribute("renovations", not(hasItem(
                        hasProperty("name", is("Renovation One"))))))
                .andExpect(content().string(containsString("No Renovations found.")))
                .andExpect(content().string(not(containsString("No Renovations have been made yet."))));
    }

    /**
     * Tests posting to the create renovations page which will create a new renovation record under the current user. If all the details
     * (name, description, rooms) are in the correct format and then posted, the user is taken to the view page for that renovation and the
     * record is added to the repository.
     * @throws Exception if the request processing fails
     */
    @Test
    public void postCreateRecord_validRecordDetails_createRecord() throws Exception {
        List<RenovationRecord> userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        assertTrue(userRecords.isEmpty());

        // New record has a name with diacritic letters and a description of length 512 to test regex and boundaries.
        mockMvc.perform(post("/renovations/create")
                        .param("name", "Rénövatiôn Onē")
                        .param("description", "A".repeat(512))
                        .param("roomList", "Room 1", "Room 2")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=6"))
                .andExpect(flash().attribute("renovation",
                        hasProperty("name", is("Rénövatiôn Onē"))));

        userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Rénövatiôn Onē");
        assertFalse(userRecords.isEmpty());
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
     * Tests deleting a renovation when the id in the link is associated with a current record. A no content response is then returned
     * to show the user the deletion was successful.
     * @throws Exception if the request processing fails
     */
    @Test
    public void deleteRecord_validRecordId_deletionSuccess() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        List<RenovationRecord> userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        assertFalse(userRecords.isEmpty());

        mockMvc.perform(delete("/renovations/delete/{id}", existingRecord.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        assertTrue(userRecords.isEmpty());
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

        List<RenovationRecord> userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        assertTrue(userRecords.isEmpty());

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

        List<RenovationRecord> userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        assertTrue(userRecords.isEmpty());

        userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Rénövatiôn Onē");
        assertFalse(userRecords.isEmpty());
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


        List<RenovationRecord> userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        assertFalse(userRecords.isEmpty());

        userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One!");
        assertTrue(userRecords.isEmpty());
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

        List<RenovationRecord> userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        assertFalse(userRecords.isEmpty());
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
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("400 BAD_REQUEST \"This renovation does not exist\"", Objects.requireNonNull(result.getResolvedException()).getMessage()));
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
                        .param("tasksPerPage", "5")
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
                        .param("tasksPerPage", "5")
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
                        .param("tasksPerPage", "5")
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
                        .param("tasksPerPage", "5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + existingRecord.getId() + "&page=2&tasksPerPage=5"));


        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("page", "-1")
                        .param("tasksPerPage", "5")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + existingRecord.getId() + "&page=1&tasksPerPage=5"));
    }
}

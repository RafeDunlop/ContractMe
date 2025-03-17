package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
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
                .andExpect(content().string(not(containsString("No Renovations found."))));;

    }

    /**
     * Tests the name search function when there are records that match with the input. Only shows the
     * records that match.
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
     * Tests the name search function when there aren't any records that match with the input.
     * Doesn't show any records and has a message saying that no records match the input.
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

    @Test
    public void postCreateRecord_validRecordDetails_createRecord() throws Exception {
        List<RenovationRecord> userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        Assertions.assertTrue(userRecords.isEmpty());

        mockMvc.perform(post("/renovations/create")
                        .param("name", "Rénövatiôn Onē")
                        .param("description", "A".repeat(512))
                        .param("roomList", "Room 1", "Room 2")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andExpect(model().attributeExists("renovation"))
                .andExpect(model().attribute("renovation",
                        hasProperty("name", is("Rénövatiôn Onē"))));

        userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Rénövatiôn Onē");
        Assertions.assertFalse(userRecords.isEmpty());
    }

    @Test
    public void postCreateRecord_invalidNameInput_stayOnForm() throws Exception {
        mockMvc.perform(post("/renovations/create")
                        .param("name", "Fail!")
                        .param("description", "")
                        .param("roomList", "Room", "Room")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationTemplate"))
                .andExpect(model().attribute("name", "Fail!"))
                .andExpect(model().attribute("description", ""))
                .andExpect(model().attribute("roomList", hasSize(2)));
    }

    @Test
    public void postCreateRecord_invalidDescriptionInput_stayOnForm() throws Exception {
        mockMvc.perform(post("/renovations/create")
                        .param("name", "Renovation One")
                        .param("description", "a".repeat(513))
                        .param("roomList", "Room", "Room")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationTemplate"))
                .andExpect(model().attribute("name", "Renovation One"))
                .andExpect(model().attribute("description", "a".repeat(513)))
                .andExpect(model().attribute("roomList", hasSize(2)));
    }

    @Test
    public void postCreateRecord_recordNameExists_stayOnForm() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        mockMvc.perform(post("/renovations/create")
                        .param("name", "Renovation One")
                        .param("description", "")
                        .param("roomList", "Room", "Room")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("createRenovationTemplate"))
                .andExpect(model().attribute("name", "Renovation One"))
                .andExpect(model().attribute("description", ""))
                .andExpect(model().attribute("roomList", hasSize(2)));
    }

    @Test
    public void deleteRecord_validRecordId_deletionSuccess() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        List<RenovationRecord> userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        Assertions.assertFalse(userRecords.isEmpty());

        mockMvc.perform(delete("/renovations/delete/{id}", existingRecord.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        userRecords = renovationRecordRepository.findByNameContainingIgnoreCase(currentUser, "Renovation One");
        Assertions.assertTrue(userRecords.isEmpty());
    }

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
}

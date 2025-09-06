package nz.ac.canterbury.seng302.homehelper.integration.controller;

import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
@ActiveProfiles("test")
public class TagControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagService tagService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TagRepository tagRepository;

    private User currentUser;
    private User owner;

    @BeforeEach
    public void setupUser() {
        currentUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(currentUser);

        owner = new User("Owner", "User", "owner@doe.com", "Password");
        owner.grantAuthority("ROLE_USER");
        userRepository.save(owner);
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

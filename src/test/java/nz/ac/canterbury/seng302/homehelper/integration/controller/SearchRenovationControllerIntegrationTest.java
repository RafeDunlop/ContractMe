package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
@ActiveProfiles("test")
class SearchRenovationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TagRepository tagRepository;

    private User currentUser;
    private User owner;
    private MockHttpSession session;

    private static Stream<Arguments> streamRenovationRecordPagination() {
        return Stream.of(
                Arguments.of("-100", "5", 4, 0),
                Arguments.of("1", "0", 20, 0),
                Arguments.of("0", "5", 4, 0)
        );
    }

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

        session = new MockHttpSession();
    }


    /**
     * Tests the renovation page when the user doesn't have any renovation records. Doesn't show any
     * records and has a message saying that no records have been made.
     * Simulates a first time user going to their renovation's page.
     * @throws Exception if the request processing fails
     */
    @Test
    void getRenovationRecord_noUserRecords_showNoRecordExist() throws Exception {
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
    void getRenovationForm_searchByName_returnMatchingRecord() throws Exception {
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
    void getRenovationRecord_searchByName_showNoRecordWithName() throws Exception {
        renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2")));

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("searchTerm", "Two"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = new ObjectMapper().readTree(result.getResponse().getContentAsString()).get("content");
        assertTrue(content.isEmpty());
    }

    /**
     * Tests that a paginated page can be selected using query parameters.
     * Verifies that the correct page is returned by specifying the number
     * of records per page.
     * @throws Exception if the request processing fails
     */
    @Test
    void getRenovationRecord_selectPage_returnsCorrectPage() throws Exception {
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
     * Tests that selecting a page out of bounds will redirect to the last page.
     */
    @ParameterizedTest
    @MethodSource("streamRenovationRecordPagination")
    void getRenovationRecord_selectInvalidValues_returnsDefaultPages(String pageParam, String cardsPerPageParam,
                                                                   int expectedTotalPages, int expectedPageLength) throws Exception {
        for (int i = 0; i < 20; i++) {
            renovationRecordRepository.save(new RenovationRecord(currentUser, "Renovation " + i, "Some words", List.of("Room 1")));
        }

        MvcResult result = mockMvc.perform(get("/renovations/retrieve")
                        .param("page", pageParam)
                        .param("cardsPerPage", cardsPerPageParam))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        assertEquals(expectedTotalPages, root.get("totalPages").asInt());
        assertEquals(expectedPageLength, root.get("number").asInt());
    }

    /**
     * Tests that requesting 0 items per page will redirect to the default of 8 items per page.
     */
    @Test
    void getRenovationRecord_zeroCardsPerPage_returns16ItemsPerPage() throws Exception {
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
     * Tests the renovation page when the user has some renovation records. It only shows that user's records.
     * Simulates a user who has used the application before going to their renovation's page.
     * @throws Exception if the request processing fails
     */
    @Test
    void getRenovationForm_withUserWithoutUser_returnUserRecord() throws Exception {
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

    @Test
    void searchRenovation_withNoMatches_returnsNoResults() throws Exception {
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
    void searchRenovation_withMatchingTerm_returnsMatchingRecords() throws Exception {
        RenovationRecord renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        renovationRecord.setPublicity(true);
        renovationRecordRepository.save(renovationRecord);

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
    void searchRenovation_withVisibilityFilter_returnsFilteredResults() throws Exception {
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
    void searchRenovation_withTermAndVisibilityFilter_returnsFilteredResults() throws Exception {
        RenovationRecord publicRenovation = new RenovationRecord(owner, "Public Renovation", "Room A", List.of("Room A"));
        publicRenovation.setPublicity(true);
        RenovationRecord privateRenovation = new RenovationRecord(owner, "Private Renovation", "Room B", List.of("Room B"));
        privateRenovation.setPublicity(false);
        renovationRecordRepository.saveAll(List.of(publicRenovation, privateRenovation));

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
    void tagSearch_withValidPublicRenovation_displaysMoreMatchingTagsFirst() throws Exception {
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
    void getSearchRenovations_withSessionAttributes_rendersSearchPage() throws Exception {
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
    void getSearchRenovations_withNoMatchingRecords_returnsEmptyResults() throws Exception {
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
    void getSearchRenovations_withMatchingRecords_returnsFilteredResults() throws Exception {
        RenovationRecord renovationRecord = new RenovationRecord(owner, "Renovation One", "Description", List.of("Room A"));
        renovationRecord.setPublicity(true);
        renovationRecordRepository.save(renovationRecord);

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
    void tagSearch_withValidPublicRenovation_displaysListOfTags() throws Exception {
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
    void tagSearch_withNoPublicRenovation_NoResults() throws Exception {
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
    void tagSearch_withValidPublicRenovation_displaysMoreMatchingTagsFirstListOfTags() throws Exception {
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
}
package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@AutoConfigureMockMvc
@SpringBootTest
public class TagSearchSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagService tagService;

    private final UserContext userContext;

    private MvcResult result;

    private List<String> searchTerms;

    public TagSearchSteps(UserContext userContext) {
        this.userContext = userContext;
    }


    @Given("the tags named {string} and {string} exist")
    public void the_tags_named_and_exist(String tag1Name, String tag2Name) {
        Tag tag1 = new Tag(tag1Name);
        Tag tag2 = new Tag(tag2Name);
        tagRepository.save(tag1);
        tagRepository.save(tag2);
    }

    @Given("a public renovation {string} exists with tags:")
    public void a_public_renovation_exists_with_tags(String recordName, io.cucumber.datatable.DataTable tagNames) {
        RenovationRecord renovationRecord = new RenovationRecord(userContext.getUser(), recordName, "", List.of());
        renovationRecord.setPublicity(true);

        if (renovationRecord.getTags() == null) {
            renovationRecord.setTags(new ArrayList<>());
        }
        List<String> tagNameList = tagNames.asList();
        for (String tagName : tagNameList) {
            Tag tag = tagService.getTag(tagName);
            renovationRecord.getTags().add(tag);
        }

        renovationRecordRepository.save(renovationRecord);
    }

    @Given("a private renovation {string} exists with tags:")
    public void a_private_renovation_exists_with_tags(String recordName, io.cucumber.datatable.DataTable tagNames) {
        RenovationRecord renovationRecord = new RenovationRecord(userContext.getUser(), recordName, "", List.of());

        if (renovationRecord.getTags() == null) {
            renovationRecord.setTags(new ArrayList<>());
        }
        List<String> tagNameList = tagNames.asList();
        for (String tagName : tagNameList) {
            Tag tag = tagService.getTag(tagName);
            renovationRecord.getTags().add(tag);
        }

        renovationRecordRepository.save(renovationRecord);
    }

    @When("I search for renovations with tags {string} and {string}")
    public void i_search_for_renovations_with_tags_and(String tag1Name, String tag2Name) throws Exception{
        result = mockMvc.perform(post("/renovations/search")
                        .param("tagNameList", tag1Name)
                        .param("tagNameList", tag2Name)
                        .param("isTagSearch", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/search?page=1"))
                .andReturn();

        result = mockMvc.perform(get("/renovations/search")
                        .session((MockHttpSession) Objects.requireNonNull(result.getRequest().getSession(false))))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationSearchTemplate"))
                .andReturn();
    }

    @Then("I should see the following renovations in order:")
    public void i_should_see_the_following_renovations_in_order(io.cucumber.datatable.DataTable recordNamesOrdered) throws Exception {
        List<String> recordNameList = recordNamesOrdered.asList();

        mockMvc.perform(get("/renovations/search")
                        .session((MockHttpSession) Objects.requireNonNull(result.getRequest().getSession(false))))
                .andExpect(model().attribute("records", hasSize(recordNameList.size())))
                .andExpect(model().attribute("records", contains(
                        recordNameList.stream()
                                .map(name -> hasProperty("name", is(name)))
                                .collect(Collectors.toList())
                )));
    }

    @Given("the tag search field is empty")
    public void the_tag_search_field_is_empty() {
        searchTerms = Collections.emptyList();
    }

    @When("I make a tag search")
    public void i_make_a_tag_search() throws Exception {
        var requestBuilder = post("/renovations/search")
                .param("isTagSearch", "true")
                .with(csrf());

        for (String tag : searchTerms) {
            requestBuilder = requestBuilder.param("tagNameList", tag);
        }

        result = mockMvc.perform(requestBuilder).andReturn();
    }

    @Then("I should see the message {string}")
    public void i_should_see_the_message(String errorMessage) throws Exception {
        String content = result.getResponse().getContentAsString();
        assert content.contains(errorMessage) : "Error message not found: " + errorMessage;
    }

    @Then("I am not redirected")
    public void i_should_not_be_redirected() {
        int status = result.getResponse().getStatus();
        assertNotEquals(302, status, "Expected no redirection, but got status 302 but was redirected");
        assertEquals(200, status, "Expected status 200 when submitting empty tag search");
    }
}

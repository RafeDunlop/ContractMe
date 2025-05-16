package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WithMockUser
@SpringBootTest
@Transactional
public class TagsSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TagRepository tagRepository;

    private String lastInput;

    private RenovationRecord currentRenovationRecord;

    private final UserContext userContext;

    private ResultActions resultActions;

    private MvcResult mvcResult;

    public TagsSteps(UserContext userContext) {
        this.userContext = userContext;
    }

    @Given("There is an existing tag named {string}")
    public void there_is_an_existing_tag_named(String tagName) {
        Tag tag = new Tag(tagName);
        tagRepository.save(tag);
    }

    @When("I type {string} into the tag input field")
    public void i_type_into_the_tag_input_field(String inputString) {
        lastInput = inputString;
    }

    @Given("I have a renovation record and I am on that page")
    public void i_have_a_renovation_record_and_i_am_on_that_page() {
        RenovationRecord newRenovationRecord = new RenovationRecord(userContext.getUser(), "Record " + System.currentTimeMillis(), "", List.of());
        renovationRecordRepository.save(newRenovationRecord);

        currentRenovationRecord = renovationRecordRepository.findExactMatchAllUsers(newRenovationRecord.getName()).orElse(null);
        assertNotNull(currentRenovationRecord);
    }

    @Given("I have a renovation record with a tag {string}")
    public void i_have_a_renovation_record_with_a_tag(String tagName) {
        Tag tag = new Tag(tagName);
        tagRepository.save(tag);
        RenovationRecord newRenovationRecord = new RenovationRecord(userContext.getUser(), "Record " + System.currentTimeMillis(), "", List.of());
        newRenovationRecord.addTag(tag);
        renovationRecordRepository.save(newRenovationRecord);

        currentRenovationRecord = renovationRecordRepository.findExactMatchAllUsers(newRenovationRecord.getName()).orElse(null);
        assertNotNull(currentRenovationRecord);
        assertTrue(currentRenovationRecord.getTags().contains(tag));
    }

    @Given("The record has 5 tags")
    public void the_record_has_5_tags() throws Exception {
        for (int i = 0; i < 5; i++) {
            Tag tag = new Tag("New Tag " + i);
            tagRepository.save(tag);

            currentRenovationRecord.addTag(tag);
        }
        renovationRecordRepository.save(currentRenovationRecord);
        RenovationRecord expectedRenovationRecord = renovationRecordRepository.findById(currentRenovationRecord.getId()).orElse(null);
        assertNotNull(expectedRenovationRecord);
        assertEquals(5, expectedRenovationRecord.getTags().size());
    }

    @When("I enter a tag {string} into the tag input field")
    public void i_enter_a_tag_into_the_tag_input_field(String tagName) throws Exception {
        resultActions = mockMvc.perform(post("/renovations/tags/add")
                .param("renovationId", currentRenovationRecord.getId().toString())
                .param("tagName",tagName)
                .with(csrf()));
    }

    @When("I go to the browse renovation page")
    public void i_go_to_the_browse_renovation_page() throws Exception {
        mvcResult = mockMvc.perform(get("/renovations/search")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I go to the view renovation page")
    public void i_go_to_the_view_renovation_page() throws Exception {
        mvcResult = mockMvc.perform(get("/renovations/view?id=" + currentRenovationRecord.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("Press the 'X' button next to the tag {string}")
    public void press_the_x_button_next_to_the_tag(String tagName) throws Exception {
        mvcResult = mockMvc.perform(patch("/renovations/tags/remove")
                        .param("renovationId", currentRenovationRecord.getId().toString())
                        .param("tagName",tagName)
                        .with(csrf()))
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Then("I should see an autocomplete list containing {string}")
    public void i_should_see_an_autocomplete_list_containing(String autocompleteTag) throws Exception {
        mvcResult = mockMvc.perform(get("/renovations/tags/autocomplete")
                        .param("partialTag", lastInput)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        assertTrue(responseBody.contains(autocompleteTag),
                "Expected response to contain tag: " + autocompleteTag);
    }

    @Then("I am told that a tag must contain one or more letters")
    public void i_am_told_that_a_tag_must_contain_one_or_more_letters() throws Exception {
        List<String> expectedErrors = List.of("Tags must contain one or more letters.");

        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + currentRenovationRecord.getId()))
                .andExpect(flash().attribute("errors", expectedErrors));
    }

    @Then("I am told that I cannot add another tag")
    public void i_am_told_that_i_cannot_add_another_tag() throws Exception {
        List<String> expectedErrors = List.of("Renovation cannot have more than 5 tags.");

        resultActions
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + currentRenovationRecord.getId()))
                .andExpect(flash().attribute("errors", expectedErrors));
    }

    @Then("The tag {string} is not added")
    public void the_tag_is_not_added(String tagName) throws Exception {
        RenovationRecord expectedRenovationRecord = renovationRecordRepository.findExactMatchAllUsers(currentRenovationRecord.getName()).orElse(null);
        assertNotNull(expectedRenovationRecord);

        List<Tag> tags = expectedRenovationRecord.getTags();
        assertTrue(tags.stream().noneMatch(tag -> tag.getTagName().equals(tagName)));
    }

    @Then("The tag {string} is on the list of tags for the renovation")
    public void the_tag_is_on_the_list_of_tags_for_the_renovation(String tagName) throws Exception {
        String content = mvcResult.getResponse().getContentAsString();
        assertTrue(content.contains(tagName.trim()));
    }

    @Then("The tag {string} is added to the list of tags for the renovation")
    public void the_tag_is_added_to_the_list_of_tags_for_the_renovation(String tagName) throws Exception {
        assertEquals(302, resultActions.andReturn().getResponse().getStatus());

        String url = resultActions.andReturn().getResponse().getRedirectedUrl();
        assertNotNull(url);

        mockMvc.perform(get(url).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(tagName)));
    }

    @Then("I should see an autocomplete list that doesn't contain {string}")
    public void i_should_see_an_autocomplete_list_that_doesnt_contain_tag_doesnt_exist(String autocompleteTag) throws Exception {
        mvcResult = mockMvc.perform(get("/renovations/tags/autocomplete")
                        .param("partialTag", lastInput)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        assertFalse(responseBody.contains(autocompleteTag),
                "Expected response to not contain tag: " + autocompleteTag);
    }

    @Then("The tag {string} is removed from the list of tags for the renovation")
    public void the_tag_is_removed_from_the_list_of_tags_for_the_renovation(String tagName) throws Exception {
        mockMvc.perform(get("/renovations/view?id=" + currentRenovationRecord.getId()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(tagName))));
    }
}

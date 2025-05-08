package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.Before;
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

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
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

    public TagsSteps(UserContext userContext) {
        this.userContext = userContext;
    }

    @Given("There is an existing tag named {string}")
    public void there_is_an_existing_tag_named(String tagName) {
        Tag tag = new Tag(tagName);
        tagRepository.save(tag);
        System.out.println("Saving tag: " + tagName);
    }

    @When("I type {string} into the tag input field")
    public void i_type_into_the_tag_input_field(String inputString) {
        lastInput = inputString;
    }

    @Given("I have a renovation record and I am on that page")
    public void i_have_a_renovation_record_and_I_am_on_that_page() {
        RenovationRecord newRenovationRecord = new RenovationRecord(userContext.getUser(), "Record " + System.currentTimeMillis(), "", List.of());
        renovationRecordRepository.save(newRenovationRecord);

        currentRenovationRecord = renovationRecordRepository.findExactMatchAllUsers(newRenovationRecord.getName()).orElse(null);
        Assertions.assertNotNull(currentRenovationRecord);
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
        Assertions.assertNotNull(expectedRenovationRecord);
        Assertions.assertEquals(5, expectedRenovationRecord.getTags().size());
    }

    @When("I enter a tag {string} into the tag input field")
    public void i_enter_a_tag_into_the_tag_input_field(String tagName) throws Exception {
        resultActions = mockMvc.perform(post("/renovations/tags/add")
                .param("renovationId", currentRenovationRecord.getId().toString())
                .param("tagName",tagName)
                .with(csrf()));
    }

    @Then("I should see an autocomplete list containing {string}")
    public void i_should_see_an_autocomplete_option_containing(String autocompleteTag) throws Exception {
        MvcResult result = mockMvc.perform(get("/renovations/tags/autocomplete")
                        .param("partialTag", lastInput)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
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
        Assertions.assertNotNull(expectedRenovationRecord);

        List<Tag> tags = expectedRenovationRecord.getTags();
        Assertions.assertNotNull(tags.stream().filter(tag -> tag.getTagName().equals(tagName)).findFirst().toString());
    }
}

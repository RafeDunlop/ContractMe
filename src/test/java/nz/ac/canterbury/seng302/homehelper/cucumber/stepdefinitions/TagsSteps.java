package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WithMockUser
@SpringBootTest
@Transactional
public class TagsSteps {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TagRepository tagRepository;
    private String lastInput;

    @Before
    public void setupSecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "jane@doe.com",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );
        SecurityContextHolder.setContext(context);
    }

    @Given("there is an existing tag named {string}")
    public void there_is_an_existing_tag_named(String tagName) {
        Tag tag = new Tag(tagName);
        tagRepository.save(tag);
    }
    @When("I type {string} into the tag input field")
    public void i_type_into_the_tag_input_field(String inputString) {
        lastInput = inputString;
    }

    @Then("I should see an autocomplete list containing {string}")
    public void i_should_see_an_autocomplete_option_containing(String autocompleteTag) throws Exception {
        MvcResult result = mockMvc.perform(get("/renovations/tags/autocomplete")
                        .param("partialTag", lastInput))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains(autocompleteTag),
                "Expected response to contain tag: " + autocompleteTag);
    }
}

package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class SharingRenovationRecordsSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository repository;

    private MvcResult result;

    private User testUser;
    private String expectedVisibility;
    private String expectedSearchTerm;

    @Given("I am logged in")
    public void i_am_logged_in() throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        String uniqueEmail = "test" + System.currentTimeMillis() + "@user.nz";
        testUser = new User("Test", "User", uniqueEmail, encoder.encode("Test123!"));
        testUser.activate();
        userRepository.save(testUser);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(testUser.getEmail(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        result = mockMvc.perform(get("/main").with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I tick the checkbox labelled make my renovation record public")
    public void i_tick_make_renovation_public() throws Exception {
        RenovationRecord record = new RenovationRecord(
                testUser,
                "My Private Renovation",
                "Test description",
                new ArrayList<>()
        );
        record.setPublicity(false);
        repository.save(record);

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        result = mockMvc.perform(post("/renovations/editPublicity/" + record.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPublic\": true}")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + record.getId()))
                .andReturn();
    }

    @When("I untick the checkbox labelled make my renovation record public")
    public void i_untick_make_renovation_private() throws Exception {
        RenovationRecord record = new RenovationRecord(
                testUser,
                "My Public Renovation",
                "Test description",
                new ArrayList<>()
        );
        record.setPublicity(true);
        repository.save(record);

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        result = mockMvc.perform(post("/renovations/editPublicity/" + record.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPublic\": false}")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + record.getId()))
                .andReturn();
    }

    @Then("It should be visible in public search results for all logged in users")
    public void it_should_be_visible_publicly() {
        Page<RenovationRecord> publicRecords = repository.findPublicRecords(null);
        assertTrue(publicRecords.getContent().stream().anyMatch(r -> r.getUser().equals(testUser)));
    }

    @Then("It should be invisible in public search results for all logged in users")
    public void it_should_be_invisible_publicly() {
        Page<RenovationRecord> publicRecords = repository.findPublicRecords(null);
        assertFalse(publicRecords.getContent().stream().anyMatch(r -> r.getUser().equals(testUser)));
    }

    @Given("there are {int} public renovation records")
    public void there_are_public_renovation_records(int count) {

        for (int i = 0; i < count; i++) {
            String email = "other" + i + "_" + System.currentTimeMillis() + "@example.com";
            User otherUser = new User("Other", "User" + i, email, "Password123!");
            otherUser.activate();
            userRepository.save(otherUser);

            RenovationRecord publicRecord = new RenovationRecord(
                    otherUser,
                    "Public Renovation " + i,
                    "Description " + i,
                    new ArrayList<>()
            );
            publicRecord.setPublicity(true);
            publicRecord.setCreatedTimestamp(java.time.LocalDateTime.now().minusDays(i));
            repository.save(publicRecord);
        }
    }

    @When("I click the browse renovations button")
    public void i_click_the_browse_renovations_button() throws Exception {
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        result = mockMvc.perform(get("/renovations/search")
                        .param("visibility", "public")
                        .param("searchTerm", "")
                        .with(csrf())
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I should be on the renovation search page")
    public void i_should_be_on_the_renovation_search_page() throws Exception {
        String viewContent = result.getResponse().getContentAsString();
        assertTrue(viewContent.contains("Public Renovation Records"));
    }

    @Then("I should see a list of public renovation records")
    public void i_should_see_list_of_public_renovations() throws Exception {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("badge bg-success"));
    }

    @Then("the renovation records should be sorted by most recent creation date first")
    public void renovations_should_be_sorted_desc() throws Exception {
        Page<RenovationRecord> records = repository.findPublicRecords(null);

        List<RenovationRecord> sorted = new ArrayList<>(records.getContent());

        sorted.sort(Comparator.comparing(
                RenovationRecord::getCreatedTimestamp,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        assertEquals(sorted, records.getContent());
    }

    @When("I click on a renovation record")
    public void i_click_on_a_renovation_record() throws Exception {
        RenovationRecord record = repository.findPublicRecords(null).getContent().get(0);
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        result = mockMvc.perform(get("/renovations/view")
                        .param("id", record.getId().toString())
                        .param("page", "1")
                        .session(session)
                        .header("Referer", "/renovations/search?visibility=" + expectedVisibility + "&searchTerm=" + expectedSearchTerm)) // <-- crucial
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I should see the details of that renovation record")
    public void i_should_see_the_details_of_the_record() throws Exception {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("View Renovation"));
    }

    @Given("I have searched for visibility: {string} and search term: {string} renovation records")
    public void i_have_searched_with_custom_filters(String visibility, String searchTerm) throws Exception {
        this.expectedVisibility = visibility;
        this.expectedSearchTerm = searchTerm;

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);

        result = mockMvc.perform(get("/renovations/search")
                        .param("visibility", visibility)
                        .param("searchTerm", searchTerm)
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I click the “Back to search results” button")
    public void i_click_back_to_search_results() throws Exception {
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        String viewContent = result.getResponse().getContentAsString();

        // Updated regex: more flexible and resilient to attribute ordering
        Pattern pattern = Pattern.compile("<a[^>]+href=\\\"(/renovations/search[^\\\"]*)\\\"[^>]*>\\s*Back\\s*</a>");
        Matcher matcher = pattern.matcher(viewContent);

        String backUrl = null;
        if (matcher.find()) {
            backUrl = matcher.group(1).replace("&amp;", "&"); // HTML decode
        }

        assertNotNull(backUrl, "Back to search results link not found in view HTML");

        result = mockMvc.perform(get(backUrl).session(session))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Given("there is at least {int} pages")
    public void there_is_at_lest_pages(Integer pages) {
        ModelAndView mav = result.getModelAndView();
        Map<String, Object> model = mav.getModel();
        int totalPages = (int) model.get("totalPages");

        assertTrue(totalPages >= pages, "Expected at least: " + pages + " pages, there is only: " + totalPages);
    }

    @Then("I should see the list of renovation records at the same search page I was on")
    public void i_should_see_same_search_results_page() throws Exception {
        String viewContent = result.getResponse().getContentAsString();

        // Check if we're on the Renovation Records page
        assertTrue(viewContent.contains("Renovation Records"));

        // Dynamically check the selected visibility option
        String expectedVisibilityOption = "<option value=\"" + expectedVisibility + "\" selected=\"selected\">";
        assertTrue(viewContent.contains(expectedVisibilityOption),
                "Expected visibility option not selected: " + expectedVisibilityOption);

        // Dynamically check the searchTerm input
        String expectedSearchTermInput = "name=\"searchTerm\" value=\"" + expectedSearchTerm + "\"";
        assertTrue(viewContent.contains(expectedSearchTermInput),
                "Expected search term input not matched: " + expectedSearchTermInput);
    }

    @Then("I should see a {string} element")
    public void i_should_see_a_element(String element) throws Exception {
        String content = result.getResponse().getContentAsString();

        assertTrue(content.contains("id=\"" + element + "\""),
                "Expected to find element with id=\"" + element + "\"");
    }

    @Then("I should not see a {string} element")
    public void i_should_not_see_a_element(String element) throws Exception{
        String content = result.getResponse().getContentAsString();

        assertFalse(content.contains("id=\"" + element + "\""),
                "Did not expect to find element with id=\"" + element + "\"");
    }


}

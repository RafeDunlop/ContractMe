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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
public class SharingRenovationRecordsSteps {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RenovationRecordRepository repository;

    private MvcResult result;
    private User testUser;
    private String expectedVisibility;
    private String expectedSearchTerm;
    private final int DEFAULT_PAGE_SIZE = 16;

    @Given("I am logged in")
    public void i_am_logged_in() throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        testUser = new User("Test", "User", "test" + System.currentTimeMillis() + "@test.com", encoder.encode("Test123!"));
        testUser.activate();
        userRepository.save(testUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                testUser.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        result = mockMvc.perform(get("/main").with(csrf())).andExpect(status().isOk()).andReturn();
    }

    @When("I tick the checkbox labelled make my renovation record public")
    public void i_tick_make_public() throws Exception {
        RenovationRecord record = new RenovationRecord(testUser, "Private", "desc", new ArrayList<>());
        record.setPublicity(false);
        repository.save(record);

        result = mockMvc.perform(post("/renovations/editPublicity/" + record.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPublic\": true}")
                        .session((MockHttpSession) result.getRequest().getSession(false)))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @When("I untick the checkbox labelled make my renovation record public")
    public void i_untick_make_private() throws Exception {
        RenovationRecord record = new RenovationRecord(testUser, "Public", "desc", new ArrayList<>());
        record.setPublicity(true);
        repository.save(record);

        result = mockMvc.perform(post("/renovations/editPublicity/" + record.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPublic\": false}")
                        .session((MockHttpSession) result.getRequest().getSession(false)))
                .andExpect(status().is3xxRedirection())
                .andReturn();
    }

    @Then("It should be visible in public search results for all logged in users")
    public void visible_in_search() {
        assertTrue(repository.findPublicRecords(null).getContent().stream().anyMatch(r -> r.getUser().equals(testUser)));
    }

    @Then("It should be invisible in public search results for all logged in users")
    public void invisible_in_search() {
        assertFalse(repository.findPublicRecords(null).getContent().stream().anyMatch(r -> r.getUser().equals(testUser)));
    }

    @Given("there are {int} public renovation records")
    public void create_public_records(int count) {
        for (int i = 0; i < count; i++) {
            User user = new User("User", "No" + i, "u" + i + System.currentTimeMillis() + "@u.com", "pass");
            user.activate();
            userRepository.save(user);
            RenovationRecord r = new RenovationRecord(user, "Public" + i, "desc", new ArrayList<>());
            r.setPublicity(true);
            r.setCreatedTimestamp(java.time.LocalDateTime.now().minusDays(i));
            repository.save(r);
        }
    }

    @When("I click the browse renovations button")
    public void browse_renovations() throws Exception {
        mockMvc.perform(get("/renovations/search")
                        .param("visibility", "public")
                        .session((MockHttpSession) result.getRequest().getSession(false))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        result = mockMvc.perform(get("/renovations/retrieve")
                        .param("visibility", "public")
                        .session((MockHttpSession) result.getRequest().getSession(false))
                        .with(csrf()))
                .andReturn();
    }

    @When("I click on page {int}")
    public void i_click_on_page(Integer page) throws Exception {
        result = mockMvc.perform(get("/renovations/retrieve")
                        .param("visibility", "public")
                        .param("page", page.toString()))
                .andReturn();
    }

    @Then("I see pagination metadata with page {int} selected and {int} total pages")
    public void i_see_pagination_metadata( int expectedPage, int expectedTotalPages) throws Exception {
        String json = result.getResponse().getContentAsString();

        assertTrue(json.contains("\"totalPages\":" + expectedTotalPages));
        assertTrue(json.contains("\"number\":" + (expectedPage - 1)));
    }

    @When("I input page {int} and confirm my choice")
    public void i_input_page_and_confirm_my_choice(int pageNum) throws Exception {
        result = mockMvc.perform(get("/renovations/retrieve")
                        .param("page", String.valueOf(pageNum))
                        .param("visibility", "public"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I see {int} records on the page")
    public void i_see_a_list_of_records_on_the_page(Integer numRecords) throws Exception {
        String json = result.getResponse().getContentAsString();

        assertTrue(json.contains("\"numberOfElements\":" + numRecords));
    }

    @Then("I should be on the renovation search page")
    public void on_search_page() throws Exception {
        assertTrue(result.getResponse().getContentAsString().contains("Public Renovation Records"));
    }

    @Then("I should see a list of public renovation records")
    public void see_list() throws Exception {
        assertTrue(result.getResponse().getContentAsString().contains("badge bg-success"));
    }

    @Then("the renovation records should be sorted by most recent creation date first")
    public void sorted_by_date() {
        List<RenovationRecord> records = repository.findPublicRecords(null).getContent();
        List<RenovationRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(RenovationRecord::getCreatedTimestamp, Comparator.nullsLast(Comparator.reverseOrder())));
        assertEquals(sorted, records);
    }

    @When("I click on a renovation record")
    public void click_renovation() throws Exception {
        RenovationRecord record = repository.findPublicRecords(null).getContent().get(0);
        result = mockMvc.perform(get("/renovations/view")
                        .param("id", record.getId().toString())
                        .param("page", "1")
                        .header("Referer", "/renovations/search?visibility=" + expectedVisibility + "&searchTerm=" + expectedSearchTerm)
                        .session((MockHttpSession) result.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I should see the details of that renovation record")
    public void see_details() throws Exception {
        assertTrue(result.getResponse().getContentAsString().contains("View Renovation"));
    }

    @Then("I see the list of public records corresponding to page {int}")
    public void i_see_the_list_of_public_records_corresponding_to_page(Integer page) throws Exception {
        String json = result.getResponse().getContentAsString();

        assertTrue(json.contains("\"offset\":" + DEFAULT_PAGE_SIZE *  (page - 1)));
        assertTrue(json.contains("\"pageNumber\":" + (page - 1)));
    }

    @Given("I have searched for visibility: {string} and search term: {string} renovation records")
    public void set_search_state(String vis, String term) throws Exception {
        this.expectedVisibility = vis;
        this.expectedSearchTerm = term;

        mockMvc.perform(get("/renovations/search")
                        .param("visibility", vis)
                        .param("searchTerm", term)
                        .session((MockHttpSession) result.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @When("I click the “Back to search results” button")
    public void click_back_button() throws Exception {
        result = mockMvc.perform(get("/renovations/search?visibility=" + expectedVisibility + "&searchTerm=" + expectedSearchTerm)
                        .session((MockHttpSession) result.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I should see the list of renovation records at the same search page I was on")
    public void see_same_results() throws Exception {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Renovation Records"));
        assertTrue(content.contains("<option value=\"" + expectedVisibility + "\" selected=\"selected\">"));
        assertTrue(content.contains("name=\"searchTerm\" value=\"" + expectedSearchTerm + "\""));
    }
}
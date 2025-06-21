package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SearchingRenovationsStepDefs {
    private final int DEFAULT_PAGE_SIZE = 8;
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    private final UserContext userContext;
    private MvcResult result;
    private String searchTerm;

    public SearchingRenovationsStepDefs(UserContext userContext) {
        this.userContext = userContext;
    }

    @Given("I have {int} records")
    public void i_have_records(Integer numRecords) {
        User user = userContext.getUser();
        for (int i = 0; i < numRecords; i++) {
            RenovationRecord renovationRecord = new RenovationRecord(user, "MyRenovation" + i, "Description", new ArrayList<>());
            renovationRecord.setCreatedTimestamp(java.time.LocalDateTime.now().minusDays(i));
            renovationRecordRepository.save(renovationRecord);
        }
        Page<RenovationRecord> records = renovationRecordRepository.findByUser(user, null);
        assertEquals(numRecords, records.getSize());
    }

    @When("I have run a search for {string}")
    public void i_have_run_a_search_for(String searchTerm) throws Exception {
        searchTerm = searchTerm;
        result = mockMvc.perform(get("/renovations")
                        .param("searchTerm", searchTerm)
                .with(csrf()))
            .andReturn();
    }

    @Then("I see a {string} button")
    public void i_see_pagination_buttons(String buttonName) throws UnsupportedEncodingException {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains(buttonName), String.format("Expected to find %s in the response", buttonName));
    }

    @Then("I see a list of {int} records")
    public void i_see_a_list_of_records(Integer numRecords) {
        Object records = Objects.requireNonNull(result.getModelAndView())
                .getModel()
                .get("records");

        assertInstanceOf(List.class, records, "Expected 'records' to be a List");

        @SuppressWarnings("unchecked")
        List<RenovationRecord> renovationRecords = (List<RenovationRecord>) records;

        assertEquals(numRecords.intValue(), renovationRecords.size(), "Record count mismatch");
    }

    @Given("I see pagination numbers for {int} pages")
    public void i_see_pagination_numbers_for_pages(Integer pageNum) throws UnsupportedEncodingException {
        String content = result.getResponse().getContentAsString();
        int numPages = (int) Objects.requireNonNull(result.getModelAndView()).getModel().get("totalPages");
        assertEquals(pageNum, numPages);
        for (int i = 0; i < numPages; i++) {
            assertTrue(content.contains(String.format("page%d", i + 1)), String.format("Expected to find page%d in the response", i + 1));
        }
    }

    @When("I click on page number {int}")
    public void i_click_on_page_number(Integer page) throws Exception {
        result = mockMvc.perform(get("/renovations")
                .param("searchTerm", searchTerm)
                .param("page", page.toString()))
            .andReturn();
    }

    @Then("I see the list of records corresponding to page {int}")
    public void i_see_the_list_of_records_corresponding_to_page(Integer page) {
        @SuppressWarnings("unchecked")
        List<RenovationRecord> records = (List<RenovationRecord>) Objects.requireNonNull(result.getModelAndView())
                .getModel()
                .get("records");

        int expectedStartIndex = DEFAULT_PAGE_SIZE * (page - 1);
        String expectedName = "MyRenovation" + expectedStartIndex;

        assertEquals(DEFAULT_PAGE_SIZE, result.getModelAndView().getModel().get("cardsPerPage"));
        assertEquals(expectedName, records.get(0).getName());
    }

    @Then("Page number {int} is currently highlighted")
    public void page_number_is_currently_highlighted(Integer int1) throws UnsupportedEncodingException {
        String content = result.getResponse().getContentAsString();
        assertTrue(content.lines().anyMatch(s -> s.contains("page" + int1) && s.contains("active")));
    }

    @When("I input page number {int} and confirm my choice")
    public void i_input_page_number_and_confirm_my_choice(int pageNum) throws Exception {
        result = mockMvc.perform(get("/renovations")
                .param("page", String.valueOf(pageNum))
                .param("searchTerm", searchTerm))
                .andExpect(status().isOk())
                .andReturn();
    }
}

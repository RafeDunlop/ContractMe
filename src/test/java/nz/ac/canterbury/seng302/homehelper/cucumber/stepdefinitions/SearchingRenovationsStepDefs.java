package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
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
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SearchingRenovationsStepDefs {
    private final int DEFAULT_PAGE_SIZE = 16;
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
        assertEquals(numRecords.intValue(), records.getTotalElements());
    }

    @When("I have run a search for {string}")
    public void i_have_run_a_search_for(String searchTerm) throws Exception {
        this.searchTerm = searchTerm;
        mockMvc.perform(get("/renovations")
                        .param("searchTerm", searchTerm)
                        .with(csrf()))
                .andReturn();
        result = mockMvc.perform(get("/renovations/retrieve")
                    .param("searchTerm", searchTerm)
                    .with(csrf()))
                .andReturn();
    }

    @Then("I see pagination metadata with {int} total pages and page {int} selected")
    public void i_see_pagination_metadata(int expectedTotalPages, int expectedPage) throws Exception {
        String json = result.getResponse().getContentAsString();

        assertTrue(json.contains("\"totalPages\":" + expectedTotalPages));
        assertTrue(json.contains("\"number\":" + (expectedPage - 1)));
    }

    @Then("I see {int} records")
    public void i_see_a_list_of_records(Integer numRecords) throws Exception {
        String json = result.getResponse().getContentAsString();

        assertTrue(json.contains("\"numberOfElements\":" + numRecords));
    }

    @When("I click on page number {int}")
    public void i_click_on_page_number(Integer page) throws Exception {
        result = mockMvc.perform(get("/renovations/retrieve")
                        .param("searchTerm", searchTerm)
                        .param("page", page.toString()))
                .andReturn();
    }

    @Then("I see the list of records corresponding to page {int}")
    public void i_see_the_list_of_records_corresponding_to_page(Integer page) throws Exception {
        String json = result.getResponse().getContentAsString();

        assertTrue(json.contains("\"offset\":" + DEFAULT_PAGE_SIZE *  (page - 1)));
        assertTrue(json.contains("\"pageNumber\":" + (page - 1)));
    }

    @When("I input page number {int} and confirm my choice")
    public void i_input_page_number_and_confirm_my_choice(int pageNum) throws Exception {
        result = mockMvc.perform(get("/renovations/retrieve")
                        .param("page", String.valueOf(pageNum))
                        .param("searchTerm", searchTerm))
                .andExpect(status().isOk())
                .andReturn();
    }
}

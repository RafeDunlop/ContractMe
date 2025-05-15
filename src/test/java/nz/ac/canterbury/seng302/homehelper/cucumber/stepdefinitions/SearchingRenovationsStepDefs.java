package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    private UserContext userContext;
    private MvcResult result;

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
        List<RenovationRecord> records = renovationRecordRepository.findByUser(user);
        assertEquals(numRecords, records.size());
    }

    @When("I have run a search for {string}")
    public void i_have_run_a_search_for(String string) throws Exception {
        result = mockMvc.perform(get("/renovations")
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
        Object records = result.getModelAndView().getModel().get("renovations");
        assertInstanceOf(List.class, records, "Expected 'renovations' to be a List");
        @SuppressWarnings("unchecked")
        List<RenovationRecord> renovationRecords = (List<RenovationRecord>) records;
        assertEquals(numRecords, renovationRecords.size());
    }

}

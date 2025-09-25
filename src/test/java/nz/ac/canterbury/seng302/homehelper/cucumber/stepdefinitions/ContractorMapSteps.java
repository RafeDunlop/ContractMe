package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.cucumber.context.UserContext;
import nz.ac.canterbury.seng302.homehelper.dto.MappedContractor;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class ContractorMapSteps {
    private Team team;
    private String skillName = "";
    private MvcResult mvcResult;
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private UserContext userContext;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;
    @Autowired
    private TeamsRepository teamsRepository;
    @Autowired
    private ContractorRepository contractorRepository;
    @Autowired
    private MockMvc mockMvc;

    @Given("I have a team with an unfilled {string} role for a renovation at {double}, {double}")
    public void i_have_a_team_with_an_unfilled_role_for_a_renovation_at(String skillName, Double latitude, Double longitude) {
        RenovationRecord record = new RenovationRecord(userContext.getUser(), "Reno", "A test reno", List.of());
        record.setLocation(new Location(
                "address",
                "country",
                "postcode",
                "city",
                "suburb",
                latitude,
                longitude
        ));
        record = renovationRecordRepository.save(record);
        team = new Team(record);
        team.addRole(new Role(Skill.valueOf(skillName)));
        team = teamsRepository.save(team);
    }

    @Given("There are contractors")
    public void there_are_contractors(List<Map<String, String>> contractors) {
        for (Map<String, String> contractorData : contractors) {
            Contractor contractor = new Contractor(contractorData.get("first_name"), contractorData.get("last_name"), contractorData.get("email"), "password");
            contractor.setSkills(Set.of(Skill.valueOf(contractorData.get("skill"))));
            contractor.setAvailable(Boolean.parseBoolean(contractorData.get("available")));
            contractor.setLocation(new Location(
                    "address",
                    "country",
                    "postcode",
                    "city",
                    "suburb",
                    Double.parseDouble(contractorData.get("latitude")),
                    Double.parseDouble(contractorData.get("longitude")))
            );
            contractorRepository.save(contractor);
        }
    }

    @When("I view the map to invite a contractor for the {string} role")
    public void i_view_the_map_to_invite_a_contractor(String skillName) throws Exception {
        this.skillName = skillName;
        mvcResult = mockMvc.perform(get("/map/eligible")
                .param("skill", skillName)
                .param("teamId", String.valueOf(team.getId())))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I see a map showing the profile pictures of")
    public void i_see_a_map_showing_the_profile_pictures_of(List<String> contractorEmails) throws UnsupportedEncodingException, JsonProcessingException {
        List<MappedContractor> expectedContractors = new ArrayList<>();
        for (String contractorEmail : contractorEmails) {
            Contractor contractor = contractorRepository.findByEmailIgnoreCase(contractorEmail).orElseThrow();
            MappedContractor mappedContractor = new MappedContractor(contractor, Skill.valueOf(skillName));
            expectedContractors.add(mappedContractor);
        }
        List<MappedContractor> actualContractors = mapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>(){});
        assertEquals(expectedContractors, actualContractors);
    }
}

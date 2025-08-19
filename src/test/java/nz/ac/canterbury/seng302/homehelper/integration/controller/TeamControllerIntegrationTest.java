package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.dto.UserRegisterDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.ContractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "jane@doe.nz")
@Transactional
public class TeamControllerIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    private RenovationRecord renovationRecord;

    @Autowired
    private ContractorService contractorService;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;


    @Autowired
    private TeamsRepository teamsRepository;


    @BeforeEach
    public void setup(TestInfo testInfo) {
        User newUser = new User("Jane", "Doe", "jane@doe.nz", "password");
        newUser = userRepository.save(newUser);
        newUser.grantAuthority("ROLE_USER");
        renovationRecord = new RenovationRecord(newUser, "test renovation", "test description", List.of());
        renovationRecord = renovationRecordRepository.save(renovationRecord);


        if (testInfo.getDisplayName().contains("hasLocation")) {
            Location location = new Location();
            location.setAddress("nonNull");
            location.setLongitude(172.580907);
            location.setLatitude(-43.522345);
            newUser.setLocation(location);
            renovationRecord.setLocation(location);
            renovationRecordRepository.save(renovationRecord);
            userRepository.save(newUser);
        }

        if (testInfo.getDisplayName().contains("assignContractors")) {
            UserRegisterDTO user = new UserRegisterDTO();
            user.setFirstName("Janet");
            user.setLastName("Doe");
            user.setPassword("P4$$word");
            user.setConfirmPassword("P4$$word");
            user.setIsContractor(true);
            user.setEmail("seng302.team200.contractor@gmail.com");
            user.setSkills(List.of(Skill.SCAFFOLDING, Skill.RESOURCE_CONSENT_COMPLIANCE, Skill.CARPENTRY));
            user.setHourlyRate(30.0f);
            user.setCountryCode(64);
            user.setPhoneNumber("33692888");
            AddressDTO address = new AddressDTO();
            address.setAddress_line1("Ilam Road");
            address.setCity("Christchurch");
            address.setRegion("Ilam");
            address.setCountry("New Zealand");
            address.setPostcode("");
            address.setLat(-43.522345);
            address.setLon(172.580907);

            List<Skill> skillList = Skill.listOfSortedSkills();

            for (int i = 1; i <= 5; i++) {
                user.setEmail("seng302.team200.contractor" + i + "@gmail.com");
                address.setAddress_line1(i + " Ilam Road");
                address.setLat(-43.522345 + i * 0.001);
                address.setLon(172.580907 + i * 0.001);
                user.setSkills(List.of(skillList.get(i)));
                Contractor newContractor = contractorService.registerContractor(user, address);
                Contractor contractor = contractorService.getContractorById(newContractor.getId());
                contractor.setAvailable(true);
                contractorRepository.save(contractor);

            }
        }
    }

    @Test
    public void teamController_hasLocationOwnsRecord_getsForm() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "different@user.nz")
    public void teamController_hasLocationDoesNotOwnRecord_returns404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void teamController_ownsRecordDoesNotHaveLocation_returns404() throws Exception {
        Location location = new Location();
        renovationRecord.setLocation(location);
        renovationRecordRepository.save(renovationRecord);
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void hasLocation_createTeam_submitsTeamWithRoles_createsTeam() throws Exception {
        mockMvc.perform(post("/renovations/team/create")
                        .param("id", renovationRecord.getId().toString())
                        .param("skills", "DRYWALL_PLASTERING")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + renovationRecord.getId()));

        boolean exists = teamsRepository.existsByRenovationRecordId(renovationRecord.getId());
        assertTrue(exists);
        assertEquals(1,teamsRepository.findByRenovationRecord(renovationRecord).getRoles().size());
    }

    @Test
    void hasLocation_createTeam_submitsTeamWithDuplicateRoles_createsTeam() throws Exception {
        mockMvc.perform(post("/renovations/team/create")
                        .param("id", renovationRecord.getId().toString())
                        .param("skills", "ELECTRICAL", "ELECTRICAL")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/renovations/view?id=" + renovationRecord.getId()));

        boolean exists = teamsRepository.existsByRenovationRecordId(renovationRecord.getId());
        assertTrue(exists);
        assertEquals(2,teamsRepository.findByRenovationRecord(renovationRecord).getRoles().size());
    }

    @Test
    public void createTeam_renovationHasTeamAndHasLocation_returns404() throws Exception {
        Team existingTeam = new Team(renovationRecord);
        teamsRepository.save(existingTeam);
        mockMvc.perform(MockMvcRequestBuilders.get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId()))
                        .param("skills", "ELECTRICAL", "PLUMBING"))
                .andExpect(status().isNotFound());
    }

    @Test
    void hasLocation_createTeam_submitsTeamWithRoles_assignContractorsToTeams() throws Exception {
        mockMvc.perform(post("/renovations/team/create")
                        .param("id", renovationRecord.getId().toString())
                        .param("skills", "ANTIQUE_RESTORATION", "ARCHITECTURE", "ASBESTOS_REMOVAL")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("response", true))
                .andReturn();


    }

    @Test
    void hasLocation_createTeam_submitsTeamWithRoles_doesNotAssignContractorsToTeams() throws Exception {
        mockMvc.perform(post("/renovations/team/create")
                        .param("id", renovationRecord.getId().toString())
                        .param("skills", "ELECTRICAL", "RESOURCE_CONSENT_COMPLIANCE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("response", false))
                .andReturn();


    }

    @Test
    @WithMockUser(username = "bob.doe@doe.nz")
    void getJoinTeamFragment_validTeam_returnsFragment() throws Exception {
        Team team = new Team(renovationRecord);
        Role role = new Role(Skill.CARPENTRY);
        Contractor contractor = new Contractor("Bob", "Doe", "bob.doe@doe.nz", "password");
        role.setContractor(contractor);
        team.addRole(role);
        contractorRepository.save(contractor);
        team = teamsRepository.save(team);
        mockMvc.perform(get("/renovations/team/join-team")
                .param("id", team.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("skill", "Carpentry"))
                .andExpect(model().attribute("renovationName", "test renovation"))
                .andExpect(model().attribute("ownerName", "Jane Doe"))
                .andExpect(model().attribute("profilePicture", "default/default.jpg"))
                .andExpect(model().attribute("teamId", team.getId()))
                .andExpect(model().attribute("renovationId", renovationRecord.getId()))
                .andExpect(view().name("fragments/joinTeam :: join-team"));
    }

}



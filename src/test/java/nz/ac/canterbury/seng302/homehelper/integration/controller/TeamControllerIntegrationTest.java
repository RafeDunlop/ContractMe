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
import nz.ac.canterbury.seng302.homehelper.service.EmailService;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @MockBean
    private EmailService emailService;

    @Autowired
    private TeamsRepository teamsRepository;
    @SpyBean
    private LocationService locationService;

    @Autowired
    LoginService loginService;

    private Location location;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        User defaultUser = new User("Jane", "Doe", "jane@doe.nz", "password");
        defaultUser = userRepository.save(defaultUser);
        defaultUser.grantAuthority("ROLE_USER");
        renovationRecord = new RenovationRecord(defaultUser, "test renovation", "test description", List.of());
        renovationRecord = renovationRecordRepository.save(renovationRecord);


        if (testInfo.getDisplayName().contains("hasLocation")) {
            location = new Location();
            location.setAddress("nonNull");
            location.setLongitude(172.580907);
            location.setLatitude(-43.522345);
            defaultUser.setLocation(location);
            renovationRecord.setLocation(location);
            renovationRecordRepository.save(renovationRecord);
            userRepository.save(defaultUser);
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
            user.setCountryCode("64");
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
                doNothing().when(locationService).injectCoordsViaGeocoding(address);
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
        mockMvc.perform(get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId()))
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "different@user.nz")
    public void teamController_hasLocationDoesNotOwnRecord_returns404() throws Exception {
        mockMvc.perform(get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId())))
                .andExpect(status().isNotFound());
    }

    @Test
    public void teamController_ownsRecordDoesNotHaveLocation_returns404() throws Exception {
        Location location = new Location();
        renovationRecord.setLocation(location);
        renovationRecordRepository.save(renovationRecord);
        mockMvc.perform(get("/renovations/team/create")
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
        mockMvc.perform(get("/renovations/team/create")
                        .param("id", Long.toString(renovationRecord.getId()))
                        .param("skills", "ELECTRICAL", "PLUMBING"))
                .andExpect(status().isNotFound());
    }

    @Test
    void hasLocation_createTeam_submitsTeamWithRoles_assignContractorsToTeamsAndSendsEmails() throws Exception {
       mockMvc.perform(post("/renovations/team/create")
                        .param("id", renovationRecord.getId().toString())
                        .param("skills", "ANTIQUE_RESTORATION", "ARCHITECTURE", "ASBESTOS_REMOVAL")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("response", true))
                .andReturn();
       //If any skills are added in the future, change this threshold to match the number of skills present
       Mockito.verify(emailService, atMost(3)).sendRequestToContractor(Mockito.anyString(), Mockito.anyString(),
               Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class),Mockito.anyLong());

    }

    @Test
    void hasLocation_createTeam_submitsTeamWithRoles_doesNotAssignContractorsToTeamsAndSendsNoEmails() throws Exception {
        mockMvc.perform(post("/renovations/team/create")
                        .param("id", renovationRecord.getId().toString())
                        .param("skills", "ELECTRICAL", "RESOURCE_CONSENT_COMPLIANCE")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("response", false))
                .andReturn();

        Mockito.verify(emailService, Mockito.never()).sendRequestToContractor(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(Locale.class),Mockito.anyLong());

    }

    @Test
    @WithMockUser(username = "bob.doe@doe.nz")
    void getJoinTeamFragment_validTeam_returnsFragment() throws Exception {
        Team team = new Team(renovationRecord);
        Role role = new Role(Skill.CARPENTRY);
        Contractor contractor = new Contractor("Bob", "Doe", "bob.doe@doe.nz", "password");
        contractor = contractorRepository.save(contractor);
        role.setContractor(contractor);
        team.addRole(role);
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

    @Test
    void deleteTeam_teamExistsAndOwnedByLoggedIn_teamDeleted() throws Exception {
        Team team = teamsRepository.save(new Team(renovationRecord));
        mockMvc.perform(delete("/renovations/team/delete/{id}", Long.toString(team.getId()))
                        .with(csrf()))
                .andExpect(status().isNoContent());
        assertNull(teamsRepository.findByRenovationRecord(renovationRecord));
    }

    @Test
    @WithMockUser(username = "other.user@doe.nz")
    void deleteTeam_teamExistsButNotOwnedByLoggedIn_404AndTeamNotDeleted() throws Exception {
        Team team = teamsRepository.save(new Team(renovationRecord));
        userRepository.save(
                new User("other", "user", "other.user@doe.nz", "dummyPassword"));
        mockMvc.perform(delete("/renovations/team/delete/{id}", Long.toString(team.getId()))
                        .with(csrf()))
                .andExpect(status().isNotFound());
        assertNotNull(teamsRepository.findByRenovationRecord(renovationRecord));
    }

    @Test
    void deleteTeam_teamDoesNotExist_404() throws Exception {
        mockMvc.perform(delete("/renovations/team/delete/{id}", 0)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTeam_possessingRoles_teamDeleted() throws Exception {
        Team team = new Team(renovationRecord);
        Role role = new Role(Skill.CARPENTRY);
        team.addRole(role);
        team = teamsRepository.save(team);
        mockMvc.perform(delete("/renovations/team/delete/{id}", Long.toString(team.getId()))
                        .with(csrf()))
                .andExpect(status().isNoContent());
        assertNull(teamsRepository.findByRenovationRecord(renovationRecord));
    }

    @Test
    void viewTeam_rendersThreeRoleCards_andShowsCorrectTexts() throws Exception {
        Team team = new Team(renovationRecord);

        //Accepted contractor
        Contractor alice = contractorRepository.save(new Contractor("Alice", "Builder", "alice@test.nz", "pw"));
        alice.setProfilePicture("alice.jpg");
        contractorRepository.save(alice);
        Role accepted = new Role(Skill.CARPENTRY);
        accepted.setContractor(alice);
        accepted.setAccepted(true);
        team.addRole(accepted);

        //Pending contractor
        Contractor bob = contractorRepository.save(new Contractor("Bob", "Spark", "bob@test.nz", "pw"));
        bob.setProfilePicture("bob.jpg");
        contractorRepository.save(bob);
        Role pending = new Role(Skill.ELECTRICAL);
        pending.setContractor(bob);
        pending.setAccepted(false);
        team.addRole(pending);

        //No contractor
        Role empty = new Role(Skill.PLUMBING);
        team.addRole(empty);

        team = teamsRepository.save(team);

        mockMvc.perform(get("/renovations/team/view")
                        .param("id", team.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewTeam"))
                .andExpect(model().attributeExists("team"))
                .andExpect(model().attributeExists("contractors"))
                .andExpect(content().string(containsString("<title>View Team</title>")))
                .andExpect(content().string(containsString("<h1 id=\"header-title\" class=\"text-black\">View Team</h1>")))
                //Accepted contractor shows full name and skill
                .andExpect(content().string(containsString("Alice Builder")))
                .andExpect(content().string(containsString("Carpentry")))
                .andExpect(content().string(containsString("/profile_pictures/alice.jpg")))
                //Pending contractor shows Invite Sent and image and skill
                .andExpect(content().string(containsString("Invite Sent!")))
                .andExpect(content().string(containsString("/profile_pictures/bob.jpg")))
                .andExpect(content().string(containsString("Electrical")))
                //Empty role shows No Contractor Found, default icon, and skill
                .andExpect(content().string(containsString("No Contractor Found")))
                .andExpect(content().string(containsString("icons/profile-icon.svg")))
                .andExpect(content().string(containsString("Plumbing")));

    }


    @Test
    void deleteContractorFromRole_validUser_deletionSuccess() throws Exception {
        Location locationBeingDeletedFrom = new Location();
        locationBeingDeletedFrom.setLatitude(-43);
        locationBeingDeletedFrom.setLongitude(43);
        renovationRecord.setLocation(locationBeingDeletedFrom);
        Team team = new Team(renovationRecord);

        Contractor alice = contractorRepository.save(new Contractor("Alice", "Builder", "alice@test.nz", "pw"));
        alice.setProfilePicture("alice.jpg");
        contractorRepository.save(alice);
        Role accepted = new Role(Skill.CARPENTRY);
        accepted.setContractor(alice);
        accepted.setAccepted(true);
        team.addRole(accepted);
        teamsRepository.save(team);


        mockMvc.perform(delete("/renovations/team/delete")
                        .param("teamId", String.valueOf(team.getId()))
                        .param("contractorId", String.valueOf(alice.getId()))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        assertNull(teamsRepository.findByRenovationRecord(renovationRecord).getRoles().get(0).getContractorId());
    }

    @Test
    void deleteContractor_invalidUserForDelete_forbiddenError() throws Exception {
        User anotherUser = userRepository.save(new User("John", "Doe", "john@doe.com", "password"));

        RenovationRecord record = renovationRecordRepository.save(
                new RenovationRecord(anotherUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"))
        );
        Team team = new Team(record);

        Contractor alice = contractorRepository.save(new Contractor("Alice", "Builder", "alice@test.nz", "pw"));
        alice.setProfilePicture("alice.jpg");
        contractorRepository.save(alice);
        Role accepted = new Role(Skill.CARPENTRY);
        accepted.setContractor(alice);
        accepted.setAccepted(true);
        team.addRole(accepted);
        teamsRepository.save(team);

        mockMvc.perform(delete("/renovations/team/delete")
                        .param("teamId", String.valueOf(team.getId()))
                        .param("contractorId", String.valueOf(alice.getId()))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void hasLocation_declineInvitation_roleRemainsEmpty() throws Exception {
        String contractorEmail = "steve" + System.nanoTime() + "@doe.com";
        Contractor contractor = new Contractor("Steve", "Doe", contractorEmail, "Password123!");
        contractor.setLocation(location);
        contractor.addSkill(Skill.PLUMBING);
        contractor.activate();
        contractor.setAvailable(true);
        contractorRepository.save(contractor);

        renovationRecord.setLocation(location);
        renovationRecordRepository.save(renovationRecord);

        Team team = new Team(renovationRecord);
        team.addRole(new Role(Skill.PLUMBING));
        team.getRoles().get(0).setContractor(contractor);
        teamsRepository.save(team);

        mockMvc.perform(post("/renovations/team/invitations/{teamId}/decline", team.getId())
                        .with(user(contractorEmail).roles("USER"))
                        .with(csrf())
        ).andExpect(status().is3xxRedirection());

        Team updated = teamsRepository.findById(team.getId()).orElseThrow();
        assertNotEquals(contractor.getId(), updated.getRoles().get(0).getContractorId(),
                "Expected role to remain empty and original contractor is not re-invited."
        );
    }
}



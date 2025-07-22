package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationTaskRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userReposoitories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WithMockUser
@AutoConfigureMockMvc
@Transactional
public class DeleteRenovationRecordSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private RenovationTaskRepository renovationTaskRepository;

    private Long renovationId;

    private User testUser;


    @Given("I am an existing user")
    public void i_am_an_existing_user() throws Exception {

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
    }

    @Given("I have an existing renovation")
    public void i_have_an_existing_renovation() {
        RenovationRecord renovationRecord = new RenovationRecord(testUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(renovationRecord);

        Optional<RenovationRecord> renovationWithId = renovationRecordRepository.findExactMatch("Renovation One", testUser);
        Assertions.assertTrue(renovationWithId.isPresent());
        Assertions.assertEquals(renovationRecord.getName(), renovationWithId.get().getName());
        renovationId = renovationWithId.get().getId();
    }

    @Given("I am on the confirmation prompt for deleting a renovation record")
    public void i_am_on_the_confirmation_prompt_for_deleting_a_renovation_record() throws Exception {
        mockMvc.perform(get("/renovations")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("renovationsTemplate"));
    }

    @Given("The renovation record has {int} task\\(s)")
    public void the_renovation_record_has_tasks(int tasks) {
        Optional<RenovationRecord> renovationRecord = renovationRecordRepository.findById(renovationId);
        if (renovationRecord.isPresent()) {
            for (int i = 0; i < tasks; i++) {
                RenovationTask renovationTask = new RenovationTask("Task " + i, "description", List.of(),
                        LocalDate.now(), renovationRecord.get());
                renovationTaskRepository.save(renovationTask);
            }
        }
    }

    @When("I click the \"Delete\" button")
    public void i_click_the_delete_button() throws Exception {
        mockMvc.perform(delete("/renovations/delete/{id}", renovationId)
                .with(csrf()));
    }

    @Then("The renovation record is permanently deleted")
    public void the_renovation_record_is_permanently_deleted() throws Exception {
        Optional<RenovationRecord> renovationRecord = renovationRecordRepository.findById(renovationId);
        Assertions.assertTrue(renovationRecord.isEmpty());
    }
}
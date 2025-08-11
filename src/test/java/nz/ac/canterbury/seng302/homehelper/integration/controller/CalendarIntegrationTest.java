package nz.ac.canterbury.seng302.homehelper.integration.controller;

import nz.ac.canterbury.seng302.homehelper.controller.RenovationController;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class CalendarIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RenovationController renovationController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private RenovationRecordService renovationRecordService;

    private User currentUser;
    private User owner;
    private User notOwner;
    private User testUser;


    private RenovationRecord renovationRecord;

    private MockHttpSession session;

    @BeforeEach
    public void setupUser() {
        currentUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(currentUser);

        owner = new User("Owner", "User", "owner@doe.com", "Password");
        owner.grantAuthority("ROLE_USER");
        userRepository.save(owner);

        notOwner = new User("Not", "Owner", "not.owner@doe.com", "Password");
        notOwner.grantAuthority("ROLE_USER");
        userRepository.save(notOwner);

        testUser = new User("Test", "User", "test@doe.com", "Password");
        testUser.grantAuthority("ROLE_USER");
        userRepository.save(testUser);

        renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        renovationRecord = renovationRecordRepository.save(renovationRecord);

        session = new MockHttpSession();
    }

    @Test
    public void viewRenovation_dateEditedInMonth_dateCellHighlighted() throws Exception {
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate dateEdited = firstOfMonth.plusDays(5);
        MvcResult result = mockMvc.perform(get("/renovations/view")
                    .session(session)
                    .param("dateEdited", dateEdited.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                    .param("id", Long.toString(renovationRecord.getId()))
                    .param("year", Integer.toString(firstOfMonth.getYear()))
                    .param("month", Integer.toString(firstOfMonth.getMonthValue())))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(result.getResponse().getContentAsString().contains("dateCell"));
        assertTrue(result.getResponse().getContentAsString().contains("#fafa91"));
    }

    @Test
    public void viewRenovation_dateEditedNotInMonth_dateCellNotPresent() throws Exception {
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate dateEdited = firstOfMonth.minusMonths(1);
        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .session(session)
                        .param("dateEdited", dateEdited.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                        .param("id", Long.toString(renovationRecord.getId()))
                        .param("year", Integer.toString(firstOfMonth.getYear()))
                        .param("month", Integer.toString(firstOfMonth.getMonthValue())))
                .andExpect(status().isOk())
                .andReturn();
        assertFalse(result.getResponse().getContentAsString().contains("dateCell"));
    }
}

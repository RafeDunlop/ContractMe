package nz.ac.canterbury.seng302.homehelper.cucumber.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class CalendarSteps {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    private MvcResult result;
    private User testUser;
    private RenovationRecord renovationRecord;
    private int calendarMonth;
    private int calendarYear;

    @Given("I have a renovation record")
    public void i_have_a_renovation_record() throws Exception{
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

        renovationRecord = new RenovationRecord(testUser, "Private", "desc", new ArrayList<>());
        renovationRecordRepository.save(renovationRecord);
    }

    @Given("I have a public renovation record")
    public void i_have_a_public_renovation_record() throws Exception {
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

        renovationRecord = new RenovationRecord(testUser, "Private", "desc", new ArrayList<>());
        renovationRecord.setPublicity(true);
        renovationRecordRepository.save(renovationRecord);
    }

    @When("I navigate to my renovation record")
    public void i_navigate_to_my_renovation_record() throws Exception {
        result = mockMvc.perform(get("/renovations/view?id=" + renovationRecord.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I see a calendar for the current month")
    public void i_see_a_calendar_for_the_current_month() throws Exception {
        String html = result.getResponse().getContentAsString();

        Assertions.assertTrue(html.contains("id=\"calendar\""));

        LocalDate now = LocalDate.now();
        String expectedMonthYear = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + now.getYear();

        Assertions.assertTrue(html.contains(expectedMonthYear));
    }

    @When("calendar year is {int} and month is {int}")
    public void calendar_year_is_and_month_is(int year, int month) throws Exception {
        calendarYear = year;
        calendarMonth = month;
        result = mockMvc.perform(get("/renovations/calendar?id=" + renovationRecord.getId()
                + "&year=" + year + "&month=" + month)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I see a button for the previous and next month")
    public void i_see_a_button_for_the_previous_and_next_month() throws Exception {
        String html = result.getResponse().getContentAsString();

        if (calendarYear < 1 || calendarMonth < 1 || calendarMonth > 12) {
            calendarYear = LocalDate.now().getYear();
            calendarMonth = LocalDate.now().getMonthValue();
        }

        Assertions.assertTrue(html.contains("id=\"calendar\""));
        LocalDate date = LocalDate.of(calendarYear, calendarMonth, 1);
        String expectedMonthYear = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + date.getYear();
        String expectedPrevMonthButton = date.minusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String expectedNextMonthButton = date.plusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        Assertions.assertTrue(html.contains(expectedMonthYear));
        Assertions.assertTrue(html.contains(expectedPrevMonthButton));
        Assertions.assertTrue(html.contains(expectedNextMonthButton));
    }

    @When("I click the previous month button")
    public void i_click_the_previous_month_button() throws Exception {
        calendarYear = calendarMonth == 1 ? calendarYear - 1 : calendarYear;
        calendarMonth = calendarMonth == 1 ? 12 : calendarMonth - 1;
        result = mockMvc.perform(get("/renovations/calendar?id=" + renovationRecord.getId()
                        + "&year=" + calendarYear + "&month=" + calendarMonth)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I see a calendar for the previous month")
    public void i_see_a_calendar_for_the_previous_month() throws Exception {
        String html = result.getResponse().getContentAsString();

        if (calendarYear < 1 || calendarMonth < 1 || calendarMonth > 12) {
            calendarYear = LocalDate.now().getYear();
            calendarMonth = LocalDate.now().getMonthValue();
        }

        Assertions.assertTrue(html.contains("id=\"calendar\""));
        LocalDate date = LocalDate.of(calendarYear, calendarMonth, 1);
        String expectedMonthYear = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + date.getYear();
        String expectedPrevMonthButton = date.minusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String expectedNextMonthButton = date.plusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        Assertions.assertTrue(html.contains(expectedMonthYear));
        Assertions.assertTrue(html.contains(expectedPrevMonthButton));
        Assertions.assertTrue(html.contains(expectedNextMonthButton));
    }

    @When("I click the next month button")
    public void i_click_the_next_month_button() throws Exception {
        calendarYear = calendarMonth == 12 ? calendarYear + 1 : calendarYear;
        calendarMonth = calendarMonth == 12 ? 1 : calendarMonth + 1;

        result = mockMvc.perform(get("/renovations/calendar?id=" + renovationRecord.getId()
                        + "&year=" + calendarYear + "&month=" + calendarMonth)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I see a calendar for the next month")
    public void i_see_a_calendar_for_the_next_month() throws Exception {
        String html = result.getResponse().getContentAsString();

        if (calendarYear < 1 || calendarMonth < 1 || calendarMonth > 12) {
            calendarYear = LocalDate.now().getYear();
            calendarMonth = LocalDate.now().getMonthValue();
        }

        Assertions.assertTrue(html.contains("id=\"calendar\""));
        LocalDate date = LocalDate.of(calendarYear, calendarMonth, 1);
        String expectedMonthYear = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + date.getYear();
        String expectedPrevMonthButton = date.minusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String expectedNextMonthButton = date.plusMonths(1).getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        Assertions.assertTrue(html.contains(expectedMonthYear));
        Assertions.assertTrue(html.contains(expectedPrevMonthButton));
        Assertions.assertTrue(html.contains(expectedNextMonthButton));
    }

    @When("second user views my private renovation record")
    public void second_user_views_my_private_renovation_record() throws Exception {
        SecurityContextHolder.clearContext();

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        User secondUser = new User("Second", "User", "seconduser@test.com", encoder.encode("Test12322!"));
        secondUser.activate();
        userRepository.save(secondUser);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                secondUser.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        result = mockMvc.perform(get("/renovations/view?id=" + renovationRecord.getId())
                        .with(user(secondUser.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("they do not see a calendar")
    public void they_do_not_see_a_calendar() throws Exception {
        String html = result.getResponse().getContentAsString();
        System.out.println(html);
        Assertions.assertFalse(html.contains("id=\"calendar\""), "Calendar should not be visible on private records to other users");
    }

    @Then("today's date is highlighted")
    public void today_s_date_is_highlighted() throws Exception {
        String html = result.getResponse().getContentAsString();
        String noWhitespaceHTML = html.replaceAll("\\s+", " ");

        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();

        String expectedHtml = String.format(
                "#87bcfa",
                day
        );

        Assertions.assertTrue(
                noWhitespaceHTML.contains(expectedHtml),
                "Expected to find current date highlighted: " + expectedHtml
        );
    }
}

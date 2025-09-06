package nz.ac.canterbury.seng302.homehelper.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import nz.ac.canterbury.seng302.homehelper.dto.CalendarCellDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.Team;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.Role;
import nz.ac.canterbury.seng302.homehelper.entity.users.Skill;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.RenovationRecordRepository;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "jane@doe.com")
@ActiveProfiles("test")
@SuppressWarnings("unchecked")
public class ViewRenovationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RenovationRecordRepository renovationRecordRepository;

    @Autowired
    private TeamsRepository teamsRepository;

    private User currentUser;
    private RenovationRecord renovationRecord;

    @BeforeEach
    public void setupUser() {
        currentUser = new User("Jane", "Doe", "jane@doe.com", "password");
        userRepository.save(currentUser);

        User owner = new User("Owner", "User", "owner@doe.com", "Password");
        owner.grantAuthority("ROLE_USER");
        userRepository.save(owner);

        renovationRecord = new RenovationRecord(owner, "Test Renovation", "Test Desc", List.of("Room A"));
        renovationRecordRepository.save(renovationRecord);
    }

    /**
     * Tests getting the view record page with an id associated with an existing record. The user is taken to the view records page for
     * that record where the renovation's details are shown.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getViewRecord_validRecordId_returnForm() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andExpect(model().attribute("renovation", existingRecord));
    }

    /**
     * Tests getting the view record page with an id not associated with any existing record. The application throws an exception,
     * and the user is not taken to the page.
     * @throws Exception if the request processing fails
     */
    @Test
    public void getViewRecord_invalidRecordId_throwException() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId() + 1))
                        .with(csrf()))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertInstanceOf(ResponseStatusException.class, result.getResolvedException()))
                .andExpect(result -> assertEquals("404 NOT_FOUND \"This renovation does not exist\"", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    @WithMockUser(username = "not.owner@example.com")
    // GitHub Copilot generated some parts of the following test
    public void getViewRecord_notOwner_notFound() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "A cool renovation", "Some words", List.of("Room foo", "Room bar"));
        existingRecord = renovationRecordRepository.save(existingRecord);
        User loggedInUser = new User("Not", "Owner", "not.owner@example.com", "password");
        userRepository.save(loggedInUser);

        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void viewRenovation_validYearAndMonth_returnFormWithInputtedMonthAndYear() throws Exception {
        int inputtedYear = 2024;
        int inputtedMonth = 12;

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 1", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(inputtedYear, returnedDate.getYear());
        Assertions.assertEquals(inputtedMonth, returnedDate.getMonthValue());
        Assertions.assertEquals(6, returnedCalendarCells.size());
    }

    @Test
    public void viewRenovation_validMonthNoYear_returnFormWithInputtedMonthAndCurrentYear() throws Exception {
        int inputtedMonth = 12;
        int currentYear = LocalDate.now().getYear();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 2", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(currentYear, returnedDate.getYear());
        Assertions.assertEquals(inputtedMonth, returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void viewRenovation_noMonthAndYear_returnFormWithCurrentMonthAndYear() throws Exception {
        LocalDate now = LocalDate.now();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 3", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(now.getYear(), returnedDate.getYear());
        Assertions.assertEquals(now.getMonthValue(), returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void viewRenovation_negativeYear_returnFormWithMonthAndPositiveYear() throws Exception {
        int inputtedMonth = 12;
        int inputtedYear = 0;
        int expectedYear = LocalDate.now().getYear();
        int expectedMonth = LocalDate.now().getMonthValue();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 4", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(expectedYear, returnedDate.getYear());
        Assertions.assertEquals(expectedMonth, returnedDate.getMonthValue());
        Assertions.assertEquals(5, returnedCalendarCells.size());
    }

    @Test
    public void viewRenovation_invalidMonth_returnFormWithCurrentMonthAndYear() throws Exception {
        int inputtedMonth = 13;
        int inputtedYear = 2025;
        LocalDate now = LocalDate.now();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 5", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(now.getYear(), returnedDate.getYear());
        Assertions.assertEquals(now.getMonthValue(), returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void calendar_validYearAndMonth_returnFormWithInputtedMonthAndYear() throws Exception {
        int inputtedYear = 2024;
        int inputtedMonth = 12;

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 1", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(inputtedYear, returnedDate.getYear());
        Assertions.assertEquals(inputtedMonth, returnedDate.getMonthValue());
        Assertions.assertEquals(6, returnedCalendarCells.size());
    }

    @Test
    public void calendar_validMonthNoYear_returnFormWithInputtedMonthAndCurrentYear() throws Exception {
        int inputtedMonth = 12;
        int currentYear = LocalDate.now().getYear();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 2", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(currentYear, returnedDate.getYear());
        Assertions.assertEquals(inputtedMonth, returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void calendar_noMonthAndYear_returnFormWithCurrentMonthAndYear() throws Exception {
        LocalDate now = LocalDate.now();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 3", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(now.getYear(), returnedDate.getYear());
        Assertions.assertEquals(now.getMonthValue(), returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void calendar_negativeYear_returnFormWithMonthAndPositiveYear() throws Exception {
        int inputtedMonth = 12;
        int inputtedYear = 0;
        int expectedYear = LocalDate.now().getYear();
        int expectedMonth = LocalDate.now().getMonthValue();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 4", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(expectedYear, returnedDate.getYear());
        Assertions.assertEquals(expectedMonth, returnedDate.getMonthValue());
        Assertions.assertEquals(5, returnedCalendarCells.size());
    }

    @Test
    public void calendar_invalidMonth_returnFormWithCurrentMonthAndYear() throws Exception {
        int inputtedMonth = 13;
        int inputtedYear = 2025;
        LocalDate now = LocalDate.now();

        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation with Calendar 5", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/calendar")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("year", String.valueOf(inputtedYear))
                        .param("month", String.valueOf(inputtedMonth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/calendar :: calendar"))
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        Assertions.assertNotNull(modelAndView, "ModelAndView should not be null");

        LocalDate returnedDate = (LocalDate) modelAndView.getModel().get("date");
        List<List<CalendarCellDTO>> returnedCalendarCells = (List<List<CalendarCellDTO>>) result.getModelAndView().getModel().get("datesArray");

        Assertions.assertEquals(now.getYear(), returnedDate.getYear());
        Assertions.assertEquals(now.getMonthValue(), returnedDate.getMonthValue());
        Assertions.assertTrue(List.of(5, 6).contains(returnedCalendarCells.size()));
    }

    @Test
    public void calendar_dateEditedPresent_modelContainsDate() throws Exception {
        String dateToReturnTo = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        renovationRecordRepository.save(existingRecord);

        MvcResult result = mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(existingRecord.getId()))
                        .param("dateEdited", dateToReturnTo)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andExpect(model().attribute("renovation", existingRecord))
                .andReturn();

        Assertions.assertTrue(Objects.requireNonNull(result.getModelAndView()).getModelMap().containsKey("dateFormatter"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    @WithMockUser(username = "contractor@test.com")
    void viewRenovation_private_contractorOnTeam_returnsOk(boolean accepted) throws Exception {
        Contractor contractor = new Contractor("Greg", "Smith", "contractor@test.com", "Password123!");
        contractor.grantAuthority("ROLE_USER");
        userRepository.save(contractor);

        renovationRecord.setPublicity(false);
        renovationRecord = renovationRecordRepository.save(renovationRecord);

        Team team = new Team(renovationRecord);
        team.addRole(new Role(contractor, Skill.ELECTRICAL, accepted));
        teamsRepository.save(team);

        mockMvc.perform(get("/renovations/view")
                        .param("id", Long.toString(renovationRecord.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("viewRenovation"))
                .andReturn();
    }

    @Test
    @WithMockUser(username = "steve@test.com")
    void viewRenovation_privateRenovation_noTeam_randomUser_4xx() throws Exception {
        User randomUser = new User("Steve", "Jacobson", "steve@test.com", "Password123!");
        userRepository.save(randomUser);

        mockMvc.perform(get("/renovations/view")
                        .param("id", renovationRecord.toString())
                        .with(csrf()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void getViewRecord_withPagination_returnPaginatedTasks_JSON() throws Exception {
        RenovationRecord existingRecord = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        List<RenovationTask> renovationTasks = IntStream.range(0, 15)
                .mapToObj(i -> new RenovationTask(
                        "Task " + i,
                        "Description for Task " + i,
                        List.of("Room 1", "Room 2"),
                        LocalDate.now().plusDays(i),
                        existingRecord
                ))
                .toList();
        existingRecord.setRenovationTasks(renovationTasks);
        renovationRecordRepository.save(existingRecord);

        ObjectMapper mapper = new ObjectMapper();

        // Page 1
        MvcResult result1 = mockMvc.perform(get("/renovations/retrieve/" + existingRecord.getId())
                        .param("page", "1")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root1 = mapper.readTree(result1.getResponse().getContentAsString());
        assertEquals(5, root1.get("content").size());
        assertEquals(0, root1.get("number").asInt());
        assertEquals(3, root1.get("totalPages").asInt());

        // Page 2
        MvcResult result2 = mockMvc.perform(get("/renovations/retrieve/" + existingRecord.getId())
                        .param("page", "2")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root2 = mapper.readTree(result2.getResponse().getContentAsString());
        assertEquals(5, root2.get("content").size());
        assertEquals(1, root2.get("number").asInt());
        assertEquals(3, root2.get("totalPages").asInt());

        // Page 3
        MvcResult result3 = mockMvc.perform(get("/renovations/retrieve/" + existingRecord.getId())
                        .param("page", "3")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root3 = mapper.readTree(result3.getResponse().getContentAsString());
        assertEquals(5, root3.get("content").size());
        assertEquals(2, root3.get("number").asInt());
        assertEquals(3, root3.get("totalPages").asInt());
    }

    @Test
    public void getRenovationTaskPages_withInvalidPageNumber_returnsDefaultOrLastPage() throws Exception {
        RenovationRecord record = new RenovationRecord(currentUser, "Renovation One", "Some words", List.of("Room 1", "Room 2"));
        List<RenovationTask> tasks = IntStream.range(0, 10)
                .mapToObj(i -> new RenovationTask(
                        "Task " + i,
                        "Description " + i,
                        List.of("Room 1", "Room 2"),
                        LocalDate.now().plusDays(i),
                        record
                ))
                .toList();
        record.setRenovationTasks(tasks);
        renovationRecordRepository.save(record);

        long recordId = record.getId();

        MvcResult outOfBoundsResult = mockMvc.perform(get("/renovations/retrieve/" + recordId)
                        .param("page", "34")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = new ObjectMapper().readTree(outOfBoundsResult.getResponse().getContentAsString());
        assertEquals(2, root.get("totalPages").asInt());
        assertEquals(1, root.get("number").asInt());

        MvcResult negativePageResult = mockMvc.perform(get("/renovations/retrieve/" + recordId)
                        .param("page", "-1")
                        .param("cardsPerPage", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode negativeRoot = new ObjectMapper().readTree(negativePageResult.getResponse().getContentAsString());
        assertEquals(0, negativeRoot.get("number").asInt()); // page 1 (0-based)
    }
}

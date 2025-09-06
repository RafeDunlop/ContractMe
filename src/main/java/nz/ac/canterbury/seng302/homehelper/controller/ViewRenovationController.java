package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.dto.CalendarCellDTO;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ViewRenovationController {

    private static final Logger logger = LoggerFactory.getLogger(ViewRenovationController.class);

    private final RenovationRecordService renovationRecordService;
    private final TeamsService teamsService;
    private final RenovationTaskService renovationTaskService;
    private final LoginService loginService;
    private final LocationService locationService;

    /**
     * Autowired constructor for the request inbox controller
     * @param renovationRecordService Service methods for the renovation records
     * @param teamsService Service methods for the teams
     * @param renovationTaskService Service methods for the tasks
     * @param loginService Service methods for getting the current user
     * @param locationService Service methods for the locations
     */
    @Autowired
    public ViewRenovationController(RenovationRecordService renovationRecordService, TeamsService teamsService, RenovationTaskService renovationTaskService, LoginService loginService, LocationService locationService) {
        this.renovationRecordService = renovationRecordService;
        this.teamsService = teamsService;
        this.renovationTaskService = renovationTaskService;
        this.loginService = loginService;
        this.locationService = locationService;
    }

    /**
     * Handles redirecting to the view record page for a given record based on the id
     *
     * @param id           of the renovation record to view
     * @param pageNumber   the page of tasks to view, defaults to 1
     * @param model        (map-like) representation of results to be used by thymeleaf
     * @return view page of the renovation
     * @throws ResponseStatusException if the renovation record does not exist
     */
    @GetMapping("/renovations/view")
    public String viewRenovation(@RequestParam(name = "id") Long id,
                                 @RequestParam(defaultValue = "1", name = "page") int pageNumber,
                                 @RequestParam(required = false) Integer year,
                                 @RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate dateEdited,
                                 Model model,
                                 HttpServletRequest request) {
        logger.info("GET /renovations/view");
        logger.info("dateEdited: {}", dateEdited);

        RenovationRecord record = renovationRecordService.getRecordById(id);
        if (record == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation does not exist");

        User user = loginService.getUserByEmail();
        boolean isOwner = user.equals(record.getUser());

        if (!isOwner && !record.isPublic() && !teamsService.checkViewRenovationAccess(record, user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation is not accessible");
        }

        List<String> iconFileNames = renovationTaskService.getTaskIconFilenames();

        String previousRenovationPage = (String) request.getSession().getAttribute("lastVisitedRenovationPage");
        String previousRenovationParameters = (String) request.getSession().getAttribute("lastVisitedRenovationParameters");

        injectDateElements(year, month, dateEdited, model, record);
        model.addAttribute("dateEdited", dateEdited);


        model.addAttribute("previousUrl", previousRenovationPage + previousRenovationParameters);
        model.addAttribute("hasLocation", locationService.hasLocation(record));
        model.addAttribute("hasTeam",teamsService.teamExists(record.getId()));
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("pageNumber", Math.max(pageNumber, 1));
        model.addAttribute("renovation", record);
        model.addAttribute("icons", iconFileNames);
        model.addAttribute("dateFormatter", DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        return "viewRenovation";
    }

    /**
     * Retrieves the calendar fragment for a renovation record based on the provided ID and optional year/month.
     * If the year or month is invalid or not provided, the current month is used
     *
     * @param id     ID of the renovation record whose calendar is being viewed
     * @param year   Optional year to generate the calendar for (>= 1)
     * @param month  Optional month to generate the calendar for (1–12). If only a month is provided, the current year is used.
     * @param model  Model used to pass attributes to the Thymeleaf calendar fragment
     * @return       Thymeleaf calendar fragment for the given renovation
     * @throws ResponseStatusException if the renovation record does not exist or is not accessible by the current user
     */
    @GetMapping("/renovations/calendar")
    public String getCalendarFragment(@RequestParam Long id,
                                      @RequestParam(required = false) Integer year,
                                      @RequestParam(required = false) Integer month,
                                      @RequestParam(required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate dateEdited,
                                      Model model) {

        logger.info("dateEdited: {}", dateEdited);
        RenovationRecord record = renovationRecordService.getRecordById(id);
        if (record == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Renovation not found");

        User user = loginService.getUserByEmail();
        boolean isOwner = user.equals(record.getUser());
        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation is not accessible");
        }

        injectDateElements(year, month, dateEdited, model, record);
        model.addAttribute("id", id);
        model.addAttribute("dateEdited", dateEdited);
        model.addAttribute("dateFormatter", DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        return "fragments/calendar :: calendar";  // return only fragment for partial update
    }

    private void injectDateElements(@RequestParam(required = false) Integer year,
                                    @RequestParam(required = false) Integer month,
                                    @RequestParam(required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate dateEdited,
                                    Model model,
                                    RenovationRecord record) {
        LocalDate localDate = LocalDate.now();
        model.addAttribute("currentDay", localDate.getDayOfMonth());
        model.addAttribute("currentMonth", localDate.getMonthValue());
        model.addAttribute("currentYear", localDate.getYear());

        if (year != null && year >= 1 && month != null) {
            try {
                localDate = LocalDate.of(year, month, 1);
            } catch (DateTimeException e) {
                logger.error(e.getMessage());
            }
        } else if (month != null && year == null) {
            try {
                localDate = LocalDate.of(localDate.getYear(), month, 1);
            } catch (DateTimeException e) {
                logger.error(e.getMessage());
            }
        } else if (dateEdited != null) {
            localDate = dateEdited;
        }

        List<List<CalendarCellDTO>> datesArray = renovationRecordService.generateCalendarCells(localDate, record);

        model.addAttribute("datesArray", datesArray);
        model.addAttribute("date", localDate);
    }

    /**
     * Retrieves a paginated list of renovation tasks for a given renovation record.
     * Ensures that only the owner or public records are accessible and adjusts pagination
     * if the requested page is out of bounds.
     *
     * @param id            the ID of the renovation record
     * @param pageNumber    the 1-based page number to retrieve (defaults to 1)
     * @param cardsPerPage  the number of tasks per page (defaults to 5, minimum is 1)
     * @return a {@link Page} of {@link RenovationTaskDTO} objects
     * @throws ResponseStatusException if the renovation does not exist or is not accessible
     */
    @GetMapping("/renovations/retrieve/{id}")
    @ResponseBody
    public Page<RenovationTaskDTO> getRenovation(@PathVariable("id") Long id,
                                                 @RequestParam(defaultValue = "1", name = "page") int pageNumber,
                                                 @RequestParam(defaultValue = "5", name = "cardsPerPage") int cardsPerPage,
                                                 @RequestParam(defaultValue = "all") String status) {
        RenovationRecord record = renovationRecordService.getRecordById(id);

        if (record == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation does not exist");
        }

        User user = loginService.getUserByEmail();
        boolean isOwner = user.equals(record.getUser());

        if (!isOwner && !record.isPublic() && !teamsService.checkViewRenovationAccess(record, user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation is not accessible");
        }

        if (cardsPerPage < 1) {
            cardsPerPage = 5;
        }

        int requestedPage = Math.max(pageNumber - 1, 0);
        Pageable pageable = PageRequest.of(requestedPage, cardsPerPage);
        Page<RenovationTask> page = renovationTaskService.returnTaskPages(record, pageable, status);

        if (requestedPage >= page.getTotalPages() && page.getTotalPages() > 0) {
            pageable = PageRequest.of(page.getTotalPages() - 1, cardsPerPage);
            page = renovationTaskService.returnTaskPages(record, pageable, status);
        }

        return page.map(RenovationTaskDTO::new);
    }
}

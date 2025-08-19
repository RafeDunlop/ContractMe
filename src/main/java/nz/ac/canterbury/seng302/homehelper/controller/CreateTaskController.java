package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.RenovationTaskDTO;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controller for the create new task page
 */
@Controller
public class CreateTaskController {

    private static final Logger logger = LoggerFactory.getLogger(CreateTaskController.class);

    private final RenovationTaskService renovationTaskService;

    private final RenovationRecordService renovationRecordService;

    private final LoginService loginService;


    /**
     * Induces spring to automatically sets up the {@code RenovationRecordService}
     *
     * @param renovationRecordService The service associated with renovation records
     * @param renovationTaskService   the service layer responsible for renovation tasks
     * @param loginService             the service for handling logging users in
     */
    @Autowired
    public CreateTaskController(RenovationRecordService renovationRecordService, RenovationTaskService renovationTaskService, LoginService loginService) {
        this.renovationRecordService = renovationRecordService;
        this.renovationTaskService = renovationTaskService;
        this.loginService = loginService;
    }

    /**
     * Gets the renovation task creation form
     *
     * @param id    Renovation record id
     * @param model Representations of params for use in thymeleaf
     * @return the create task HTML page
     */
    @GetMapping("renovations/view/create")
    public String createTask(@RequestParam(name = "id") Long id,
                             @RequestParam(name = "fromDate", required = false) @DateTimeFormat(pattern="dd-MM-yyyy") LocalDate date,
                             Model model) {
        logger.info("GET renovations/view/create");

        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        }

        User user = loginService.getUserByEmail();
        if (renovationRecord.getUser() != user) {
            return "redirect:/main";
        }

        model.addAttribute("renovation", renovationRecord);
        model.addAttribute("roomList", renovationRecord.getRooms());
        model.addAttribute("fromDate", (date == null) ? "" : DateTimeFormatter.ofPattern("dd-MM-yyyy").format(date));


        if (!model.containsAttribute("renovationTaskDTO")) {
            RenovationTaskDTO renovationTaskDTO = new RenovationTaskDTO(
                    "",
                    "",
                    (date == null) ? "" : DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date),
                    List.of()
            );
            model.addAttribute("renovationTaskDTO", renovationTaskDTO);
        }

        return "createTaskTemplate";
    }



    /**
     * Handles the submission of the renovation task creation form.
     * Attempt to create a new renovation task using the provided form data.
     * If successful, it redirects to the renovation view page.
     * If an error occurs during task creation, it redirects back to the task creation page
     * with error messages and previously entered form data.
     *
     * @param renovationTaskDTO  The data transfer object containing form data for the new renovation task.
     * @param roomList           The list of rooms selected from the form. If null, defaults to all rooms from the renovation record.
     * @param renovationId       The ID of the renovation record this task is associated with.
     * @param redirectAttributes Flash attributes used to pass data across the redirect in case of form submission errors.
     * @return A redirect string to either the renovation view page on success or back to the create task page on failure.
     */
    @PostMapping("renovations/view/create")
    public String submitNewTask(@ModelAttribute("renovationTaskDTO") RenovationTaskDTO renovationTaskDTO,
                                @RequestParam(name = "roomList", required = false) List<String> roomList,
                                @RequestParam(name = "renovationId") Long renovationId,
                                @RequestParam(required = false, defaultValue = "") String dateToReturnTo,
                                RedirectAttributes redirectAttributes) {
        logger.info("POST renovations/view/create");
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(renovationId);
        if (Objects.equals(renovationTaskDTO.getDueDate(), "")) {
            renovationTaskDTO.setDueDate(null);
        }


        String formattedDueDate = renovationTaskService.parseDueDate(renovationTaskDTO);
        if (formattedDueDate != null) {
            redirectAttributes.addFlashAttribute("dueDate", formattedDueDate);
        }

        Map<String, List<String>> errors = renovationTaskService.validateTaskDetails(renovationTaskDTO, renovationRecord);


        if (!errors.isEmpty()) {
            errors.forEach(redirectAttributes::addFlashAttribute);

            redirectAttributes.addFlashAttribute("renovationTaskDTO", renovationTaskDTO);
            redirectAttributes.addFlashAttribute("roomList", roomList);
            redirectAttributes.addFlashAttribute("renovationId", renovationId);

            return "redirect:/renovations/view/create?id=" + renovationId;
        }

        renovationTaskService.addRenovationTask(renovationTaskDTO, renovationRecord);
        return (dateToReturnTo.isEmpty()) ?
                String.format("redirect:/renovations/view?id=%s", renovationId) :
                String.format("redirect:/renovations/view?id=%s&dateEdited=%s#cellEdited", renovationId, dateToReturnTo);
    }

}

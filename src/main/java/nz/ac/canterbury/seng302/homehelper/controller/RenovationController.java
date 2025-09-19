package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.profanityFilter.ProfanityFilter;
import nz.ac.canterbury.seng302.homehelper.service.LocationService;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UrlPathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Controller for /renovation and subsidiary endpoints, associated with the consuming of renovations
 * @author Abhisekh Chand
 */
@Controller
@RequestMapping("/renovations")
public class RenovationController {

    private static final Logger logger = LoggerFactory.getLogger(RenovationController.class);

    private final RenovationRecordService renovationRecordService;
    private final LoginService loginService;
    private final LocationService locationService;

    /**
     * Induces spring to automatically set up the {@code RenovationRecordService}
     *
     * @param renovationRecordService The renovation service which provides non-UI functionality
     * @param loginService            The login service provides the function to get the current user
     * @param locationService         The location service provides the function to validate the locations
     */
    @Autowired
    public RenovationController(RenovationRecordService renovationRecordService, LoginService loginService,
                                LocationService locationService) {
        this.renovationRecordService = renovationRecordService;
        this.loginService = loginService;
        this.locationService = locationService;
    }

    /**
     * Gets all renovations
     *
     * @param searchTerm optional string to search on renovation name (partial matching)
     * @param model      (map-like) representation of results to be used by thymeleaf
     * @return thymeleaf renovationsTemplate
     */
    @GetMapping
    public String renovations(@RequestParam(value = "searchTerm", required = false, defaultValue = "") String searchTerm,
                              @RequestParam(defaultValue = "1", name = "page") Integer pageNumber,
                              Model model,
                              HttpServletRequest request) {
        logger.info("GET renovations");

        if (pageNumber == null) pageNumber = 1;

        User user = loginService.getUserByEmail();

        String lastVisitedRenovationPage = new UrlPathHelper().getPathWithinApplication(request);
        request.getSession().setAttribute("lastVisitedRenovationPage", lastVisitedRenovationPage);
        request.getSession().setAttribute("lastVisitedRenovationParameters", request.getQueryString() != null ? "?" + request.getQueryString() : "");

        model.addAttribute("user", user);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("pageNumber", pageNumber);
        return "renovationsTemplate";
    }

    /**
     * Gets the renovation creation form
     *
     * @return thymeleaf createRenovationTemplate
     */
    @GetMapping("/create")
    public String record(@ModelAttribute AddressDTO addressDTO, Model model, HttpServletRequest request) {
        logger.info("GET /renovations/create");
        String previousRenovationPage = (String) request.getSession().getAttribute("lastVisitedRenovationPage");
        String previousRenovationParameters = (String) request.getSession().getAttribute("lastVisitedRenovationParameters");
        model.addAttribute("previousUrl", previousRenovationPage + previousRenovationParameters);
        return "createRenovationTemplate";
    }

    /**
     * Posts a form response with the renovation attributes, to be turned into a renovation
     * If the form is valid:
     * <ul>
     *     <li>creates a {@code RenovationRecord} for it and saves it to the database</li>
     *     <li>sets the {@code model} attributes for and sends the user to the viewing page for their new renovation</li>
     * </ul>
     * If the form is not valid
     * <ul>
     *     <li>sends the user back to the creation form</li>
     *     <li>sets the fields of the form to what they were at submission time</li>
     *     <li>sets some additional parameters so the client-side JavaScript can provide useful and dynamic error reporting to the user</li>
     * </ul>
     *
     * @param name        Name of the renovation
     * @param description The description of the renovation
     * @param addressDTO, dto containing renovation location details
     * @return thymeleaf createRenovationTemplate OR viewRenovationTemplate
     */
    @PostMapping("/create")
    public String submitRecord(@RequestParam(name = "name") String name,
                               @RequestParam(name = "description", required = false, defaultValue = "") String description,
                               @RequestParam(name = "roomList", required = false) List<String> roomList,
                               @ModelAttribute AddressDTO addressDTO,
                               RedirectAttributes redirectAttributes) {
        logger.info("POST /renovations/create");

        if (roomList == null) roomList = new ArrayList<>(); //cannot be a default value as technically non-constant
        Map<String, List<String>> errors = renovationRecordService.validateAllInputsCreate(name, description, roomList);

        boolean locationProvided = locationService.isLocationProvided(addressDTO);

        Location location = new Location();
        if (locationProvided) {
            errors.putAll(locationService.validateLocation(addressDTO));
            location = locationService.validateGeolocation(addressDTO, errors);
        }

        if (!errors.isEmpty()) {
            // Add each error to a flash attribute, categorising by error type
            errors.forEach(redirectAttributes::addFlashAttribute);
            redirectAttributes.addFlashAttribute("name", name);
            redirectAttributes.addFlashAttribute("description", description);
            redirectAttributes.addFlashAttribute("roomList", roomList);
            redirectAttributes.addFlashAttribute("addressDTO", addressDTO);
            redirectAttributes.addFlashAttribute("locationUsed", locationProvided);

            return "redirect:/renovations/create";
        }

        try { // save the record and go to the view page
            User user = loginService.getUserByEmail();
            try {
                RenovationRecord renovationRecord = new RenovationRecord(user, name, description, roomList);

                renovationRecordService.addRenovationRecord(renovationRecord);
                renovationRecordService.addRenovationLocation(renovationRecord, location);
                redirectAttributes.addFlashAttribute("renovation", renovationRecord);
                return "redirect:/renovations/view?id=" + renovationRecord.getId();

            } catch (IllegalArgumentException e) {
                logger.warn("Form submission error {}", e.getMessage());
                redirectAttributes.addFlashAttribute("name", name);
                redirectAttributes.addFlashAttribute("description", description);
                redirectAttributes.addFlashAttribute("roomList", roomList);
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/renovations/create";
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Deletes the renovation record by its id, redirects back to my record page
     *
     * @param id of the record to be deleted
     * @return response based on whether the record id exists, if the user doesn't have permission to delete the record, or
     * if the deletion was successful
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        logger.info("DELETE /renovations/");
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null) {
            return ResponseEntity.notFound().build();
        }
        if (!loginService.getUserByEmail().equals(renovationRecord.getUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        renovationRecordService.removeRenovationRecord(id);
        return ResponseEntity.noContent().build();
    }
}

package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for /renovation and subsidiary endpoints, associated with the consuming of renovations
 * @author Abhisekh Chand
 */
@Controller
public class RenovationController {

    private static final Logger logger = LoggerFactory.getLogger(RenovationController.class);

    private final RenovationRecordService renovationRecordService;

    /**
     * induces spring to automatically sets up the {@code RenovationRecordService}
     * @param renovationRecordService The renovation service which provides non-UI functionality
     */
    @Autowired
    public RenovationController(RenovationRecordService renovationRecordService) {
        this.renovationRecordService = renovationRecordService;
    }

    /**
     * Gets all renovations
     * @param name optional string to search on renovation name (partial matching)
     * @param model (map-like) representation of results to be used by thymeleaf
     * @return thymeleaf renovationsTemplate
     */
    @GetMapping("/renovations")
    public String renovations(@RequestParam(value = "name", required = false) String name, Model model) {
        logger.info("GET renovations");
        model.addAttribute("renovations", renovationRecordService.getRecordResult(name));
        return "renovationsTemplate";
    }

    /**
     * Gets the renovation creation form
     * @return thymeleaf createRenovationTemplate
     */
    @GetMapping("/renovations/create")
    public String record() {
        logger.info("GET /renovations/create");
        return "createRenovationTemplate";
    }

    /**
     * Posts a form response with the renovation attributes, to be turned into a renovation
     * If the form is valid:
     * <ul>
     *     <li>creates a {@code RenovationRecord} for it and saves it to the database</li>
     *     <li>sets the {@code model} attributes for and sends teh user to the viewing page for their new renovation</li>
     * </ul>
     * If the form is not valid
     * <ul>
     *     <li>sends the user back to the creation form</li>
     *     <li>sets the fields of the form to what they were at submission time</li>
     *     <li>sets some additional parameters so the client-side javascript can provide useful and dynamic error reporting to the user</li>
     * </ul>
     * @param name Name of the renovation
     * @param description The description of the renovation
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     *              with values being set to relevant parameters provided
     * @return thymeleaf createRenovationTemplate OR viewRenovationTemplate
     */
    @PostMapping("/renovations/create")
    public String submitRecord(@RequestParam(name="name") String name,
            @RequestParam(name = "description", required=false, defaultValue = "") String description,
            @RequestParam(name = "roomList", required = false) List <String> roomList,
            Model model) {
        logger.info("POST /renovations/create");
        if (roomList == null) roomList = new ArrayList<>(); //cannot be a default value as technically non-constant
        if (!renovationRecordService.validateAllInputsCreate(name, description, roomList)) {
            if (renovationRecordService.checkForExactMatch(name)) {
                model.addAttribute("existingName", name);
            }
            model.addAttribute("name", name); //otherwise the error is displayed automatically on the client side (just stop the submission)
            model.addAttribute("description", description);
            model.addAttribute("roomList", roomList);
            return "createRenovationTemplate";
        }

        try { // save the record and go to the view page
            RenovationRecord renovationRecord = new RenovationRecord(name, description, roomList);
            renovationRecordService.addRenovationRecord(renovationRecord);
            model.addAttribute("renovation", renovationRecord);
            return "viewRenovation";
        } catch (IllegalArgumentException e) {
            logger.warn("Form submission error", e);
            model.addAttribute("name", name);
            model.addAttribute("description", description);
            model.addAttribute("roomList", roomList);
            model.addAttribute("errorMessage", "Invalid input: " + e.getMessage());
            return "createRenovationTemplate";
        }
    }

    /**
     * Deletes renovation record by its id, redirects back to my records page
     * @param id of the record to be deleted
     * @return redirect to my records page
     */
    @PostMapping("/delete-renovation")
    public String deleteRecord(@RequestParam("id") Long id) {
        renovationRecordService.removeRenovationRecord(id);
        return "redirect:/renovations";
    }

    /**
     * Gets the renovation editing form
     * @param id The id of the renovation to be edited
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     * with values being set to relevant parameters provided
     * @return Thymeleaf editRenovationTemplate
     */
    @GetMapping("/renovations/edit")
    public String editRenovation(@RequestParam(name = "id") Long id, Model model) {
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        model.addAttribute("renovation", renovationRecord);
        model.addAttribute("name", renovationRecord.getName());
        return "editRenovationTemplate";
    }

    /**
     * Posts a form corresponding with an attempt to save changes to an existing renovation record
     * This form is valid if:
     * <ul>
     *     <li>The {@code id} specified corresponds to an existing renovation; and</li>
     *     <li>
     *         all the fields are valid for the conditions specified for form creation {@see product backlog},
     *         except that the name may correspond to the pre-existing name of this renovation
     *     </li>
     * </ul>
     * If the form is valid:
     * <ul>
     *      <li>updates the existing {@code RenovationRecord} saves it to the database</li>
     *      <li>sets the {@code model} attributes for and sends the user to the viewing page for their updated renovation</li>
     * </ul>
     * If the form is not valid
     * <ul>
     *      <li>sends the user back to the editing form</li>
     *      <li>sets the fields of the form to what they were at submission time</li>
     *      <li>sets some additional parameters so the client-side javascript can provide useful and dynamic error reporting to the user</li>
     * </ul>
     * @param id of the record to be edited
     * @param name of the record to be edited from the form field
     * @param description of the record to be edited from the form field
     * @param roomList list of rooms of the record to be edited from the form
     * @param model (map-like) representation of results to be used by thymeleaf
     * @return redirect to the view page for the edited record
     */
    @PostMapping("/renovations/edit")
    public String submitRenovationEdit(@RequestParam(name = "id") Long id,
                                       @RequestParam(name="name", required = false) String name,
                                       @RequestParam(name = "description", required = false) String description,
                                       @RequestParam(name = "roomList", required = false) List <String> roomList,
                                       Model model) {
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        if (roomList == null) roomList = new ArrayList<>(); //cannot be a default value as technically non-constant
        renovationRecord.setDescription(description);
        renovationRecord.setRooms(roomList);
        model.addAttribute("renovation", renovationRecord);
        boolean changesAreValid = renovationRecordService.validateAllInputsEdit(renovationRecord, name);
        if (changesAreValid) { //go to view page
            renovationRecord.setName(name); // don't set the name until the changes are valid to avoid db divergence
            renovationRecordService.addRenovationRecord(renovationRecord); //updates existing record (identified by id)
            return "viewRenovation";
        }
        if (renovationRecordService.checkForExactMatch(name, renovationRecord)) {
            model.addAttribute("existingName", name);
        }
        model.addAttribute("renovation", renovationRecord);
        model.addAttribute("name", name);
        return "editRenovationTemplate";
    }

    /**
     * Handles redirecting to the view record page for a given record based on the id
     * @param id of the renovation record to view
     * @param model (map-like) representation of results to be used by thymeleaf
     * @return redirect to viewRenovation page
     */
    @GetMapping("/renovations/view")
    public String viewRenovation(@RequestParam(name = "id") Long id, Model model) {
        RenovationRecord record = renovationRecordService.getRecordById(id);
        if (record == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        model.addAttribute("renovation", record);
        return "viewRenovation";
    }
}

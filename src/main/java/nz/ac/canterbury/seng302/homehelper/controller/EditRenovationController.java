package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.dto.AddressDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class EditRenovationController {

    private static final Logger logger = LoggerFactory.getLogger(EditRenovationController.class);

    private final RenovationRecordService renovationRecordService;
    private final LoginService loginService;
    private final LocationService locationService;

    private final String RENOVATION = "renovation";

    /**
     * Autowired constructor for the request inbox controller
     * @param renovationRecordService Service methods for the renovation records
     * @param loginService Service methods for getting the current user
     * @param locationService Service methods for the locations
     */
    @Autowired
    public EditRenovationController(RenovationRecordService renovationRecordService, LoginService loginService, LocationService locationService) {
        this.renovationRecordService = renovationRecordService;
        this.loginService = loginService;
        this.locationService = locationService;
    }

    /**
     * Gets the renovation editing form
     *
     * @param id    The id of the renovation to be edited
     * @param addressDTO the dto containing data relating to fields in address form.
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     *              with values being set to relevant parameters provided
     * @return Thymeleaf editRenovationTemplate
     */
    @GetMapping("/renovations/edit")
    public String editRenovation(@RequestParam(name = "id") Long id,
                                 @ModelAttribute AddressDTO addressDTO,
                                 Model model) {
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");

        User user = loginService.getUserByEmail();
        if (!renovationRecord.getUser().equals(user)) {
            return "redirect:/main";
        }

        // Only add the renovation object if not already present (e.g. from flash attributes)
        if (!model.containsAttribute("name")) {
            model.addAttribute(RENOVATION, renovationRecord);
        }

        if (!locationService.isLocationProvided(addressDTO)) {
            Location location = renovationRecord.getLocation();
            if (locationService.hasLocation(renovationRecord)) {
                addressDTO.setFromLocation(location);
                model.addAttribute("locationUsed", true);
            }
            model.addAttribute("addressDTO", addressDTO);
        }


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
     *      <li>sets some additional parameters so the client-side JavaScript can provide useful and dynamic error reporting to the user</li>
     * </ul>
     *
     * @param id                 of the record to be edited
     * @param name               of the record to be edited from the form field
     * @param description        of the record to be edited from the form field
     * @param roomList           list of rooms for the record to be edited from the form
     * @param redirectAttributes (map-like) representation of results to be used by thymeleaf
     * @param addressDTO the dto containing data relating to fields in address form.
     * @return redirect to the view page for the edited record
     */
    @PostMapping("/renovations/edit")
    public String submitRenovationEdit(@RequestParam(name = "id") Long id,
                                       @RequestParam(name = "name", required = false) String name,
                                       @RequestParam(name = "description", required = false) String description,
                                       @RequestParam(name = "roomList", required = false) List<String> roomList,
                                       @ModelAttribute AddressDTO addressDTO,
                                       RedirectAttributes redirectAttributes) {
        logger.info("POST /renovations/edit");

        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        if (roomList == null) roomList = new ArrayList<>(); //cannot be a default value as technically non-constant

        renovationRecord.setDescription(description);
        renovationRecord.setRooms(roomList);

        Map<String, List<String>> errors = renovationRecordService.validateAllInputsEdit(renovationRecord, name);

        Location currentLocation = renovationRecord.getLocation();
        errors.putAll(locationService.validateLocation(addressDTO));

        if (!errors.isEmpty()) {
            errors.forEach(redirectAttributes::addFlashAttribute);

            redirectAttributes.addFlashAttribute("id", id);
            redirectAttributes.addFlashAttribute("name", name);
            redirectAttributes.addFlashAttribute("description", description);
            redirectAttributes.addFlashAttribute("roomList", roomList);
            redirectAttributes.addFlashAttribute(RENOVATION, renovationRecord);
            redirectAttributes.addFlashAttribute("addressDTO", addressDTO);
            if (currentLocation != null || locationService.isLocationProvided(addressDTO)) {
                redirectAttributes.addFlashAttribute("locationUsed", true);
            }

            return "redirect:/renovations/edit?id=" + renovationRecord.getId();
        }

        renovationRecord.setName(name); // don't set the name until the changes are valid to avoid db divergence

        redirectAttributes.addFlashAttribute(RENOVATION, renovationRecord);

        renovationRecordService.updateRenovationLocation(renovationRecord, addressDTO); //updates existing record (identified by id)
        return "redirect:/renovations/view?id=" + renovationRecord.getId();
    }

    /**
     * Updates the publicity status of a renovation record.
     *
     * @param id      the ID of the renovation record
     * @param payload a JSON map containing the new publicity status
     * @return a redirect URL to the updated renovation view
     */
    @PostMapping("/renovations/editPublicity/{id}")
    public String submitPublicity(@PathVariable("id") Long id, @RequestBody Map<String, Boolean> payload) {
        logger.info("editPublicity/{id}");
        boolean isPublic = payload.get("isPublic");
        User user = loginService.getUserByEmail();
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (!renovationRecord.getUser().equals(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Action not allowed.");
        }
        renovationRecordService.changePublicity(isPublic, renovationRecord);
        return "redirect:/renovations/view?id=" + renovationRecord.getId();
    }
}

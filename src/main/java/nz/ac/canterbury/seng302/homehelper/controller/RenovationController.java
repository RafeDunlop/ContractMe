package nz.ac.canterbury.seng302.homehelper.controller;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.RenovationTask;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationTaskService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
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
    private final RenovationTaskService renovationTaskService;
    private final LoginService loginService;
    private final TagService tagService;

    /**
     * induces spring to automatically sets up the {@code RenovationRecordService}
     *
     * @param renovationRecordService The renovation service which provides non-UI functionality
     * @param loginService            The login service provides the function to get the current user
     */
    @Autowired
    public RenovationController(RenovationRecordService renovationRecordService, LoginService loginService, RenovationTaskService renovationTaskService, TagService tagService) {
        this.renovationRecordService = renovationRecordService;
        this.renovationTaskService = renovationTaskService;
        this.loginService = loginService;
        this.tagService = tagService;
    }

    /**
     * Gets all renovations
     *
     * @param searchQuery optional string to search on renovation name (partial matching)
     * @param model       (map-like) representation of results to be used by thymeleaf
     * @return thymeleaf renovationsTemplate
     */
    @GetMapping
    public String renovations(@RequestParam(value = "searchQuery", required = false, defaultValue = "") String searchQuery,
                              @RequestParam(defaultValue = "1", name = "page") int pageNumber,
                              @RequestParam(defaultValue = "8", name = "itemsPerPage") int itemsPerPage,
                              Model model) {
        logger.info("GET renovations");
        try {
            if (itemsPerPage < 1) {
                itemsPerPage = 8;
            }
            if (pageNumber < 1) {
                return "redirect:/renovations?page=1&itemsPerPage=" + itemsPerPage;
            }

            User user = loginService.getUserByEmail();
            Pageable pageable = PageRequest.of(pageNumber - 1, itemsPerPage);
            Page<RenovationRecord> renovationRecords = renovationRecordService.getPaginatedUserRecords(user, searchQuery, pageable);
            int totalPages = renovationRecords.getTotalPages();
            long totalRenovations = renovationRecords.getTotalElements();
            int paginationLinksStart = Math.max(pageNumber - 2, 1);
            int paginationLinksEnd = Math.min(pageNumber + 2, totalPages);

            if (pageNumber > totalPages && totalRenovations != 0) {
                return "redirect:/renovations?page=" + totalPages + "&itemsPerPage=" + itemsPerPage;
            }
            model.addAttribute("renovations", renovationRecords.getContent());
            model.addAttribute("paginationLinksStart", paginationLinksStart);
            model.addAttribute("paginationLinksEnd", paginationLinksEnd);
            model.addAttribute("pageNumber", pageNumber);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("itemsPerPage", itemsPerPage);
            model.addAttribute("searchQuery", searchQuery);
            return "renovationsTemplate";
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Gets the renovation creation form
     *
     * @return thymeleaf createRenovationTemplate
     */
    @GetMapping("/create")
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
     *
     * @param name        Name of the renovation
     * @param description The description of the renovation
     * @return thymeleaf createRenovationTemplate OR viewRenovationTemplate
     */
    @PostMapping("/create")
    public String submitRecord(@RequestParam(name = "name") String name,
                               @RequestParam(name = "description", required = false, defaultValue = "") String description,
                               @RequestParam(name = "roomList", required = false) List<String> roomList,
                               RedirectAttributes redirectAttributes) {
        logger.info("POST /renovations/create");

        if (roomList == null) roomList = new ArrayList<>(); //cannot be a default value as technically non-constant
        Map<String, List<String>> errors = renovationRecordService.validateAllInputsCreate(name, description, roomList);


        if (!errors.isEmpty()) {
            // Add each error to a flash attribute, categorizing by error type
            errors.forEach(redirectAttributes::addFlashAttribute);

            redirectAttributes.addFlashAttribute("name", name);
            redirectAttributes.addFlashAttribute("description", description);
            redirectAttributes.addFlashAttribute("roomList", roomList);

            return "redirect:/renovations/create";
        }

        try { // save the record and go to the view page
            User user = loginService.getUserByEmail();
            try {
                RenovationRecord renovationRecord = new RenovationRecord(user, name, description, roomList);

                renovationRecordService.addRenovationRecord(renovationRecord);
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
     * Deletes renovation record by its id, redirects back to my records page
     *
     * @param id of the record to be deleted
     * @return response based on whether the record id exists, if the user doesn't have permission to delete the record, or
     * if the deletion was successful
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        logger.info("DELETE /renovations/");
        RenovationRecord record = renovationRecordService.getRecordById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        if (!loginService.getUserByEmail().equals(record.getUser())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        renovationRecordService.removeRenovationRecord(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets the renovation editing form
     *
     * @param id    The id of the renovation to be edited
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     *              with values being set to relevant parameters provided
     * @return Thymeleaf editRenovationTemplate
     */
    @GetMapping("/edit")
    public String editRenovation(@RequestParam(name = "id") Long id, Model model) {
        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");

        User user = loginService.getUserByEmail();
        if (!renovationRecord.getUser().equals(user)) {
            return "redirect:/main";
        }

        // Only add the renovation object if not already present (e.g. from flash attributes)
        if (!model.containsAttribute("name")) {
            model.addAttribute("renovation", renovationRecord);
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
     *      <li>sets some additional parameters so the client-side javascript can provide useful and dynamic error reporting to the user</li>
     * </ul>
     *
     * @param id                 of the record to be edited
     * @param name               of the record to be edited from the form field
     * @param description        of the record to be edited from the form field
     * @param roomList           list of rooms of the record to be edited from the form
     * @param redirectAttributes (map-like) representation of results to be used by thymeleaf
     * @return redirect to the view page for the edited record
     */
    @PostMapping("/edit")
    public String submitRenovationEdit(@RequestParam(name = "id") Long id,
                                       @RequestParam(name = "name", required = false) String name,
                                       @RequestParam(name = "description", required = false) String description,
                                       @RequestParam(name = "roomList", required = false) List<String> roomList,
                                       RedirectAttributes redirectAttributes) {
        logger.info("POST /renovations/edit");

        RenovationRecord renovationRecord = renovationRecordService.getRecordById(id);
        if (renovationRecord == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This renovation does not exist");
        if (roomList == null) roomList = new ArrayList<>(); //cannot be a default value as technically non-constant

        renovationRecord.setDescription(description);
        renovationRecord.setRooms(roomList);

        Map<String, List<String>> errors = renovationRecordService.validateAllInputsEdit(renovationRecord, name);

        if (!errors.isEmpty()) {
            errors.forEach(redirectAttributes::addFlashAttribute);

            redirectAttributes.addFlashAttribute("id", id);
            redirectAttributes.addFlashAttribute("name", name);
            redirectAttributes.addFlashAttribute("description", description);
            redirectAttributes.addFlashAttribute("roomList", roomList);
            return "redirect:/renovations/edit?id=" + renovationRecord.getId();
        }

        renovationRecord.setName(name); // don't set the name until the changes are valid to avoid db divergence
        renovationRecordService.addRenovationRecord(renovationRecord); //updates existing record (identified by id)

        redirectAttributes.addFlashAttribute("renovation", renovationRecord);
        return "redirect:/renovations/view?id=" + renovationRecord.getId();
    }

    /**
     * Updates the publicity status of a renovation record.
     *
     * @param id      the ID of the renovation record
     * @param payload a JSON map containing the new publicity status
     * @return a redirect URL to the updated renovation view
     */
    @PostMapping("/editPublicity/{id}")
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

    /**
     * Handles redirecting to the view record page for a given record based on the id
     *
     * @param id           of the renovation record to view
     * @param pageNumber   the page of tasks to view, defaults to 1
     * @param model        (map-like) representation of results to be used by thymeleaf
     * @return view page of the renovation
     * @throws ResponseStatusException if the renovation record does not exist
     */
    @GetMapping("/view")
    public String viewRenovation(@RequestParam(name = "id") Long id,
                                 @RequestParam(defaultValue = "1", name = "page") int pageNumber,
                                 Model model) {
        logger.info("GET /renovations/view");

        Object cardsPerPageObj = model.asMap().get("cardsPerPage");
        Integer cardsPerPage = (cardsPerPageObj instanceof Integer) ? (Integer) cardsPerPageObj : null;
        if (cardsPerPage == null || cardsPerPage < 1) {
            cardsPerPage = 5;
        }

        Object fromSearchObj = model.asMap().get("fromSearch");
        Boolean fromSearch = (fromSearchObj instanceof Boolean) ? (Boolean) fromSearchObj : false;

        RenovationRecord record = renovationRecordService.getRecordById(id);
        if (record == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation does not exist");

        User user = loginService.getUserByEmail();
        boolean isOwner = user.equals(record.getUser());
        if (!isOwner && !record.isPublic()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation is not accessible");
        }

        if (pageNumber < 1)
            return "redirect:/renovations/view?id=" + id + "&page=1";

        int totalCards = record.getRenovationTasks().size();
        int totalPages = (totalCards + cardsPerPage - 1) / cardsPerPage;

        if (pageNumber > totalPages && totalCards != 0)
            return "redirect:/renovations/view?id=" + id + "&page=" + totalPages;

        Pageable pageable = PageRequest.of(pageNumber - 1, cardsPerPage);
        Page<RenovationTask> paginatedTasks = renovationTaskService.returnTaskPages(record, pageable);
        List<String> iconFileNames = renovationTaskService.getTaskIconFilenames();

        int paginationLinksStart = Math.max(pageNumber - 2, 1);
        int paginationLinksEnd = Math.min(pageNumber + 2, totalPages);

        model.addAttribute("isOwner", isOwner);
        model.addAttribute("fromSearch", fromSearch);
        model.addAttribute("tasks", paginatedTasks.getContent());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("renovation", record);
        model.addAttribute("paginationLinksStart", paginationLinksStart);
        model.addAttribute("paginationLinksEnd", paginationLinksEnd);
        model.addAttribute("cardsPerPage", cardsPerPage);
        model.addAttribute("totalCards", totalCards);
        model.addAttribute("icons", iconFileNames);

        return "viewRenovation";
    }

    /**
     * Handles the submission of a renovation view request. Redirects to the GET view endpoint
     * for a specific renovation record, including the requested page number and cards per page.
     * The number of cards per page and search origin flag are added as flash attributes for use
     * in the redirected view.
     *
     * @param id                 the ID of the renovation record to view
     * @param pageNumber         the page number to display (default 1)
     * @param cardsPerPage       the number of cards to display per page (defaults 5)
     * @param fromSearch         a flag indicating whether the view was triggered from a search or not
     * @param redirectAttributes used to store flash attributes for the redirect
     * @return a redirect to the GET view endpoint with query parameters for the ID and page number
     */
    @PostMapping("/view")
    public String postViewRenovation(@RequestParam(name = "id") Long id,
                                     @RequestParam(defaultValue = "1", name = "page") int pageNumber,
                                     @RequestParam(defaultValue = "5", name = "cardsPerPage") int cardsPerPage,
                                     @RequestParam(name = "fromSearch", required = false, defaultValue = "false") boolean fromSearch,
                                     RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("cardsPerPage", cardsPerPage);
        redirectAttributes.addFlashAttribute("fromSearch", fromSearch);

        return "redirect:/renovations/view?id=" + id + "&page=" + pageNumber;
    }

    /**
     * Handles the submission of a new tag to be created, and adding to renovation records
     *
     * @param renovationId       id of the renovation record
     * @param tagName            of the tag
     * @param redirectAttributes attributes for redirect
     * @return the redirect to the view page for the renovation record.
     */
    @PostMapping("/tags/add")
    public String addTagToRenovation(@RequestParam Long renovationId,
                                     @RequestParam("tagName") String tagName,
                                     RedirectAttributes redirectAttributes) {
        logger.info("/tags/add");

        RenovationRecord record = renovationRecordService.getRecordById(renovationId);
        List<String> errors = tagService.validateTagAndRecord(record, tagName);
        if (errors.isEmpty()) {
            if (tagService.checkExists(tagName)) {
                tagService.createTag(tagName);
            }
            tagService.addTagToRenovation(record, tagName);
        } else {
            redirectAttributes.addFlashAttribute("errors", errors);
        }
        return "redirect:/renovations/view?id=" + renovationId;
    }


    /**
     * Gets an autocomplete list of tag names that partially match the input
     * For AJAX requests
     *
     * @param partialTag the partial input of a tag from the user
     * @return a list of matching tag names
     */
    @GetMapping("/tags/autocomplete")
    @ResponseBody
    public List<String> autocompleteTags(@RequestParam("partialTag") String partialTag) {
        return tagService.autocompleteTags(partialTag);
    }

    /**
     * Handles the GET request to display the renovation search page. If the model does not already contain
     * renovation records, it retrieves all renovation records accessible to the current user and sets
     * default attributes for visibility, search term, and user information.
     *
     * @param model the model used to populate attributes for the view
     * @return the name of the view template for searching renovations
     */
    @GetMapping("/search")
    public String searchRenovations(Model model, HttpSession session,
                                    @RequestParam(name = "page", required = false) Integer pageNumber) {
        logger.info("GET /renovations/search");

        Map<String, Object> attributes = model.asMap();

        String visibility = (String) attributes.getOrDefault("visibility", session.getAttribute("visibility"));
        if (visibility == null) visibility = "all";
        String searchTerm = (String) attributes.getOrDefault("searchTerm", session.getAttribute("searchTerm"));
        if (searchTerm == null) searchTerm = "";
        if (pageNumber == null) {
            pageNumber = (Integer) session.getAttribute("pageNumber");
            if (pageNumber == null) pageNumber = 1;
        }

        Object cardsPerPageObj = attributes.get("cardsPerPage");
        if (cardsPerPageObj == null) {
            cardsPerPageObj = session.getAttribute("cardsPerPage");
        }
        Integer cardsPerPage = (cardsPerPageObj instanceof Integer) ? (Integer) cardsPerPageObj : null;
        if (cardsPerPage == null) cardsPerPage = 16;

        User user = loginService.getUserByEmail();

        List<RenovationRecord> records = switch (visibility.toLowerCase()) {
            case "public" -> renovationRecordService.getPublicRecords(searchTerm);
            case "user" -> renovationRecordService.getUserRecords(user, searchTerm);
            default -> renovationRecordService.getAllRecords(user, searchTerm);
        };

        if (pageNumber < 1)
            return "redirect:/renovations/search?page=1";

        int totalCards = records.size();
        int totalPages = (totalCards + cardsPerPage - 1) / cardsPerPage;

        if (pageNumber > totalPages && totalCards != 0)
            return "redirect:/renovations/search?page=" + totalPages;

        Pageable pageable = PageRequest.of(pageNumber - 1, cardsPerPage);
        Page<RenovationRecord> paginatedRecords = renovationRecordService.returnRecordPages(pageable, records);

        int paginationLinksStart = Math.max(pageNumber - 2, 1);
        int paginationLinksEnd = Math.min(pageNumber + 2, totalPages);

        model.addAttribute("visibility", visibility);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("user", user);
        model.addAttribute("records", paginatedRecords.getContent());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("paginationLinksStart", paginationLinksStart);
        model.addAttribute("paginationLinksEnd", paginationLinksEnd);
        model.addAttribute("cardsPerPage", cardsPerPage);
        model.addAttribute("totalCards", totalCards);

        return "renovationSearchTemplate";
    }

    /**
     * Handles the submission of a renovation search form. Filters renovation records based on the provided
     * visibility setting and optional search term. The filtered records, search term, and visibility
     * are added to redirect attributes and redirected to the search view.
     *
     * @param visibility         the visibility filter to apply ("public", "user", or "all")
     * @param searchTerm         an optional term to search within renovation records
     * @param session            HttpSession for storing attributes of most recent search
     * @return a redirect to the GET search endpoint with the results stored in flash attributes
     */
    @PostMapping("/search")
    public String submitSearchRenovations(@RequestParam(required = false) String visibility,
                                          @RequestParam(required = false) String searchTerm,
                                          @RequestParam(defaultValue = "1", name = "page") int pageNumber,
                                          @RequestParam(defaultValue = "16", name = "cardsPerPage") int cardsPerPage,
                                          HttpSession session) {
        logger.info("POST /renovations/search");

        if (visibility == null) visibility = "all";
        if (searchTerm == null) searchTerm = "";

        session.setAttribute("visibility", visibility);
        session.setAttribute("searchTerm", searchTerm);
        session.setAttribute("pageNumber", pageNumber);
        session.setAttribute("cardsPerPage", cardsPerPage);

        return "redirect:/renovations/search?page=" + pageNumber;
    }
}

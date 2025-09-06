package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.dto.RenovationRecordDTO;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UrlPathHelper;

import java.util.Collections;
import java.util.List;

@Controller
public class SearchRenovationController {

    private static final Logger logger = LoggerFactory.getLogger(SearchRenovationController.class);

    private final RenovationRecordService renovationRecordService;
    private final LoginService loginService;
    private final TagService tagService;

    @Autowired
    public SearchRenovationController(RenovationRecordService renovationRecordService, LoginService loginService, TagService tagService) {
        this.renovationRecordService = renovationRecordService;
        this.loginService = loginService;
        this.tagService = tagService;
    }

    /**
     * Handles the GET request to display the renovation search page. If the model does not already contain
     * renovation records, it retrieves all renovation records accessible to the current user and sets
     * default attributes for visibility, search term, and user information.
     *
     * @param model the model used to populate attributes for the view
     * @return the name of the view template for searching renovations
     */
    @GetMapping("/renovations/search")
    public String searchRenovations(Model model,
                                    @RequestParam(required = false) String visibility,
                                    @RequestParam(required = false) String searchTerm,
                                    @RequestParam(name = "tagNameList", required = false) List<String> tagNameList,
                                    @RequestParam(name = "page", required = false) String page,
                                    HttpServletRequest request) {
        logger.info("GET /renovations/search");

        if (visibility == null) visibility = "all";
        if (searchTerm == null)  searchTerm  = "";
        if (tagNameList == null) tagNameList = Collections.emptyList();

        int pageNumber;
        try {
            pageNumber = Integer.parseInt(page);
            if (pageNumber < 1) pageNumber = 1;
        } catch (Exception e) {
            pageNumber = 1;
        }

        User user = loginService.getUserByEmail();

        String lastVisitedRenovationPage = new UrlPathHelper().getPathWithinApplication(request);
        request.getSession().setAttribute("lastVisitedRenovationPage", lastVisitedRenovationPage);
        request.getSession().setAttribute("lastVisitedRenovationParameters",
                request.getQueryString() != null ? "?" + request.getQueryString() : "");

        model.addAttribute("visibility", visibility);
        model.addAttribute("searchTerm",  searchTerm);
        model.addAttribute("tagList",      tagNameList);
        model.addAttribute("user",         user);
        model.addAttribute("pageNumber",   pageNumber);
        return "renovationSearchTemplate";
    }

    /**
     * Retrieves renovation records in a paginated format based on visibility, search term, and selected tags.
     * Results are filtered, sorted by relevance, and then paginated.
     *
     * @param visibility     the scope of visibility ("public", "user", or "all"); defaults to "all"
     * @param searchTerm     an optional term to search by name or description
     * @param tagNameList    optional list of tag names used for filtering
     * @param pageNumber     the 1-based page number to retrieve (defaults to 1)
     * @param cardsPerPage   number of cards per page (defaults to 16)
     * @return a {@link Page} of {@link RenovationRecordDTO} matching the filters
     */
    @GetMapping("/renovations/retrieve")
    @ResponseBody
    public Page<RenovationRecordDTO> getRenovations(@RequestParam(required = false) String visibility,
                                                    @RequestParam(required = false) String searchTerm,
                                                    @RequestParam(name = "tagNameList", required = false) List<String> tagNameList,
                                                    @RequestParam(defaultValue = "1", name = "page") int pageNumber,
                                                    @RequestParam(defaultValue = "16", name = "cardsPerPage") int cardsPerPage,
                                                    HttpServletRequest request) {

        // Apply default values
        if (visibility == null) visibility = "all";
        if (searchTerm == null) searchTerm = "";

        // Convert tag names to Tag entities if provided
        List<Tag> tagList = (tagNameList != null) ? tagService.getTags(tagNameList) : null;

        User user = loginService.getUserByEmail();

        request.getSession().setAttribute("lastVisitedRenovationParameters", request.getQueryString() != null ? "?" + request.getQueryString() : "");

        int requestedPage = Math.max(pageNumber - 1, 0);
        cardsPerPage = Math.max(cardsPerPage, 1);
        Pageable pageable = PageRequest.of(requestedPage, cardsPerPage);

        Page<RenovationRecordDTO> page = switch (visibility.toLowerCase()) {
            case "public" -> renovationRecordService.getPaginatedPublicRecords(searchTerm, tagList, pageable);
            case "user" -> renovationRecordService.getPaginatedUserRecords(user, searchTerm, tagList, pageable);
            default -> renovationRecordService.getPaginatedVisibleRecords(user, searchTerm, tagList, pageable);
        };

        // If the requested page exceeds total pages, return the last available page
        if (requestedPage >= page.getTotalPages() && page.getTotalPages() > 0) {
            pageable = PageRequest.of(page.getTotalPages() - 1, cardsPerPage);
            page = switch (visibility.toLowerCase()) {
                case "public" -> renovationRecordService.getPaginatedPublicRecords(searchTerm, tagList, pageable);
                case "user" -> renovationRecordService.getPaginatedUserRecords(user, searchTerm, tagList, pageable);
                default -> renovationRecordService.getPaginatedVisibleRecords(user, searchTerm, tagList, pageable);
            };
        }

        return page;
    }
}

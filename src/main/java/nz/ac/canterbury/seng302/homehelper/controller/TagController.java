package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.profanityFilter.ProfanityFilter;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class TagController {

    private static final Logger logger = LoggerFactory.getLogger(TagController.class);

    public final RenovationRecordService renovationRecordService;
    public final LoginService loginService;
    public final TagService tagService;

    /**
     * Autowired constructor for the request inbox controller
     * @param renovationRecordService Service methods for the renovation records
     * @param loginService Service methods for getting the current user
     * @param tagService Service methods for the tags
     */
    @Autowired
    public TagController(RenovationRecordService renovationRecordService, LoginService loginService, TagService tagService) {
        this.renovationRecordService = renovationRecordService;
        this.loginService = loginService;
        this.tagService = tagService;
    }

    /**
     * Handles the submission of a new tag to be created and adding to renovation records
     *
     * @param renovationId       id of the renovation record
     * @param tagName            of the tag
     * @param redirectAttributes attributes for redirect
     * @return the redirect to the view page for the renovation record.
     */
    @PostMapping("/renovations/tags/add")
    public String addTagToRenovation(@RequestParam Long renovationId,
                                     @RequestParam("tagName") String tagName,
                                     @RequestParam(defaultValue = "1", name = "page") int pageNumber,
                                     RedirectAttributes redirectAttributes) {
        logger.info("POST renovations/tags/add");

        RenovationRecord record = renovationRecordService.getRecordById(renovationId);
        if (record == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation does not exist");
        else if (record.getUser() != loginService.getUserByEmail())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You don't own this renovation");
        List<String> errors = tagService.validateTagAndRecord(record, tagName);
        if (errors.isEmpty()) {
            if (tagService.checkExists(tagName)) {
                tagService.createTag(tagName);
            }
            tagService.addTagToRenovation(record, tagName);
        } else {
            redirectAttributes.addFlashAttribute("errors", errors);
            redirectAttributes.addFlashAttribute("submittedTag",tagName);
        }
        return "redirect:/renovations/view?id=" + renovationId + "&page=" + pageNumber;
    }

    /**
     * Removes the specified tag from the specified renovation. If the renovation has no such tag, there is no result
     * @param renovationId The id of the renovation to remove the tag from
     * @param tagName The name of the tag to be removed
     */
    @PatchMapping("/renovations/tags/remove")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeTagFromRenovation(@RequestParam Long renovationId, @RequestParam String tagName) {
        logger.info("PATCH renovations/tags/remove");
        RenovationRecord record = renovationRecordService.getRecordById(renovationId);
        Tag tag = tagService.getTag(tagName);
        if (record == null || tag == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "This renovation does not exist");
        else if (record.getUser() != loginService.getUserByEmail())
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You don't own this renovation");
        tagService.removeTagFromRenovation(record, tag);
    }


    /**
     * Gets an autocomplete list of tag names that partially match the input
     * For AJAX requests
     *
     * @param partialTag the partial input of a tag from the user
     * @return a list of matching tag names
     */
    @GetMapping("/renovations/tags/autocomplete")
    @ResponseBody
    public List<String> autocompleteTags(@RequestParam("partialTag") String partialTag) {
        return tagService.autocompleteTags(partialTag);
    }

    @GetMapping("/renovations/tags/profanity-filter")
    @ResponseBody
    public boolean tagProfanityFilter(@RequestParam("tagName") String tagName) {
        ProfanityFilter profanityFilter = ProfanityFilter.getInstance();
        return profanityFilter.find("en", tagName) != null;
    }
}

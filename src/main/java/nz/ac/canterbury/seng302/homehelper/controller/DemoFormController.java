package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.FormResult;
import nz.ac.canterbury.seng302.homehelper.service.FormService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller for form example.
 * Note the @link{Autowired} annotation giving us access to the @lnik{FormService} class automatically
 */
@Controller
public class DemoFormController {
    Logger logger = LoggerFactory.getLogger(DemoFormController.class);

    private final FormService formService;

    @Autowired
    public DemoFormController(FormService formService) {
        this.formService = formService;
    }
    /**
     * Gets form to be displayed, includes the ability to display results of previous form when linked to from POST form
     * @param displayName previous name entered into form to be displayed
     * @param displayLanguage previous favourite programming language entered into form to be displayed
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf
     * @return thymeleaf demoFormTemplate
     */
    @GetMapping("/form")
    public String form(@RequestParam(name="displayName", required = false, defaultValue = "") String displayName,
                       @RequestParam(name="displayFavouriteLanguage", required = false, defaultValue = "") String displayLanguage,
                       @RequestParam(name="errorMessages", required = false) String errorMessage,
                       Model model) {
        logger.info("GET /form");
        model.addAttribute("displayName", displayName.trim());
        model.addAttribute("displayFavouriteLanguage", displayLanguage.trim());
        model.addAttribute("isJava", displayLanguage.equalsIgnoreCase("java"));
        model.addAttribute("errorMessage", errorMessage); // Make sure errors persist

        return "demoFormTemplate";
    }

    /**
     * Posts a form response with name and favourite language
     * @param name name if user
     * @param favouriteLanguage users favourite programming language
     * @param model (map-like) representation of name, language and isJava boolean for use in thymeleaf,
     *              with values being set to relevant parameters provided
     * @return thymeleaf demoFormTemplate
     */
    @PostMapping("/form")
    public String submitForm( @RequestParam(name="name") String name,
                              @RequestParam(name = "favouriteLanguage") String favouriteLanguage,
                              Model model) {
        logger.info("POST /form");
        try {
            formService.addFormResult(new FormResult(name, favouriteLanguage));
            model.addAttribute("displayName", name);
            model.addAttribute("displayFavouriteLanguage", favouriteLanguage);
            model.addAttribute("isJava", favouriteLanguage.equalsIgnoreCase("java"));
        } catch (IllegalArgumentException e) {
            logger.warn("Form submission error: " + e.getMessage());
            model.addAttribute("name", name.trim());
            model.addAttribute("favouriteLanguage", favouriteLanguage.trim());
            model.addAttribute("errorMessage", "Invalid input: " + e.getMessage());
        }
        return "demoFormTemplate";
    }

    /**
     * Gets all form responses
     * @param name optional string to search on name (partial matching)
     * @param model (map-like) representation of results to be used by thymeleaf
     * @return thymeleaf demoResponseTemplate
     */
    @GetMapping("/form/responses")
    public String responses(@RequestParam(value = "name", required = false) String name, Model model) {
        logger.info("GET /form/responses");
        model.addAttribute("responses", formService.getFormResults(name));
        return "demoResponsesTemplate";
    }
}

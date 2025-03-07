package nz.ac.canterbury.seng302.homehelper.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Basic controller for fragment example
 */
@Controller
public class DemoFragmentController {
    Logger logger = LoggerFactory.getLogger(DemoFragmentController.class);

    /**
     * Gets the basic fragment example home page
     * @return thymeleaf fragmentExample/fragments
     */
    @GetMapping("/fragments/main")
    public String getFragmentHome() {
        logger.info("GET /fragments/main");
        return "fragmentExample/fragments.html";
    }

    /**
     * Gets the params fragment example page
     * @return thymeleaf fragmentExample/params
     */
    @GetMapping("/fragments/params")
    public String paramsPage() {
        logger.info("GET /fragments/params");
        return "fragmentExample/params.html";
    }

}

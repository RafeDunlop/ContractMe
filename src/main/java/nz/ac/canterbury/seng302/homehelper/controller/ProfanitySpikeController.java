package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.validation.ProfanitySpikeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfanitySpikeController {

    private static final Logger logger = LoggerFactory.getLogger(ProfanitySpikeController.class);

    private final ProfanitySpikeValidation profanitySpikeValidation;

    @Autowired
    public ProfanitySpikeController(ProfanitySpikeValidation profanitySpikeValidation) {
        this.profanitySpikeValidation = profanitySpikeValidation;

    }

   /* @GetMapping("profanitySpike")
    public String testForm(Model model) {
        logger.info("GET /profanitySpike");

    }*/
}

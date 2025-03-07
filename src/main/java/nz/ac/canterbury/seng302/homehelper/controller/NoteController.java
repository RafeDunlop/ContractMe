package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.Note;
import nz.ac.canterbury.seng302.homehelper.service.NoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Partially implemented controller to implement to 'get your feet wet' with Spring and Thymeleaf
 */
@Controller
public class NoteController {
    Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }


    @GetMapping("/notes")
    public String getNotesHome(Model model) {
        logger.info("GET /notes");
        // todo: add code to return all notes to be displayed
        return "";
    }


    @PostMapping("/notes")
    public String addNote(@RequestParam String title, @RequestParam String content, Model model) {
        logger.info("POST /notes");
        // todo: add code to save a note
        return "";
    }


    
    
    
    
}

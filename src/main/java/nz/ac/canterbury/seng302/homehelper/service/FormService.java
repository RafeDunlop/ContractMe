package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.FormResult;
import nz.ac.canterbury.seng302.homehelper.repository.FormRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for FormResults, defined by the @link{Service} annotation.
 * This class links automatically with @link{FormRepository}, see the @link{Autowired} annotation below
 */
@Service
public class FormService {
    private final FormRepository formRepository;

    @Autowired
    public FormService(FormRepository formRepository) {
        this.formRepository = formRepository;
    }


    /**
     * Gets all FormResults from persistence
     * @param name string to search on name (partial matching)
     * @return all FormResults currently saved in persistence
     */
    public List<FormResult> getFormResults(String name) {
        if (name == null || name.trim().isEmpty()) {
            return formRepository.findAll();
        }
        return formRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Adds a formResult to persistence
     * @param formResult object to persist
     * @return the saved formResult object
     */
    public FormResult addFormResult(FormResult formResult) throws IllegalArgumentException{
        List<String> errors = new ArrayList<>();

        if (formResult.getName() == null || formResult.getName().trim().isEmpty()) {
            errors.add("Please provide a valid name.");
        }
        if (formResult.getLanguage() == null || formResult.getLanguage().trim().isEmpty()) {
            errors.add("Please provide a valid language.");
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", errors));
        }

        formResult.setCreatedTimestamp(LocalDateTime.now());
        return formRepository.save(formResult);
    }
}

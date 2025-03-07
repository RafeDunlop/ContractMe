package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.Note;
import nz.ac.canterbury.seng302.homehelper.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<Note> getNotes(){
        // todo: implement get notes function
        return null;
    }

    public Note createNote(Note note) throws IllegalArgumentException {
        // todo: implement create note function with validation
        return null;
    }

}

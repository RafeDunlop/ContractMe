package nz.ac.canterbury.seng302.homehelper.service;

public class LocationNotFoundException extends RuntimeException{
    public LocationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

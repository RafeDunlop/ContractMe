package nz.ac.canterbury.seng302.homehelper.validation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TagValidation {

    //Please note that the below is only the core of the profanity checker
    //that would be built if custom implementation is selected;
    //leetspeak checking, safelist confirmation and miscellaneous 'similar words'
    //checking will all be added to ensure full compliance with ACs

    /**
     * @param tagName the string the user entered in the tag submission box,
     *                can be one or more words separated by whitespace
     * @return A list of errors that the inputted tag name generated
     */
    public List<String> validateTagName(String tagName) {
        List<String> errors = new ArrayList<>();

        List<String> profaneList = new ArrayList<>(); //Will be replaced with call to 3rd party wordlist


        String[] tagNameWords = tagName.split(" ");
        for (String word : tagNameWords) {
            for (String profaneWord: profaneList) {
                Pattern pattern = Pattern.compile(word);
                Matcher matcher = pattern.matcher(profaneWord);
                if (matcher.matches()) {
                    errors.add("This tag name does not comply with Home Helper's language standards");
                    break;
                }
            }
        }
        return errors;
    }


}



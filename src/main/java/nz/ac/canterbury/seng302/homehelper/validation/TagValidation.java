package nz.ac.canterbury.seng302.homehelper.validation;

import nz.ac.canterbury.seng302.homehelper.util.TextFileReader;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TagValidation {



    /**
     * @param tagName the string the user entered in the tag submission box,
     *                can be one or more words separated by whitespace
     * @return A list of errors that the inputted tag name generated
     */
    public List<String> validateTagName(String tagName) {
        List[] textFileReturn = TextFileReader.readTextFileReturnStringList("resources/third-party-cc-4.0/Profane-Words-English");

        List<String> profaneList = textFileReturn[0];
        List<String> errors = textFileReturn[1];

        if (!errors.isEmpty()) {
            return errors;
        }

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



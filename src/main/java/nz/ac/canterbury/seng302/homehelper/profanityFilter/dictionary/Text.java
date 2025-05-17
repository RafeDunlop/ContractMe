package nz.ac.canterbury.seng302.homehelper.profanityFilter.dictionary;

import java.util.regex.Pattern;

/**
 * The {@code Text} class provides utility methods for processing and normalizing text,
 * particularly for the purposes of profanity detection.
 */
public class Text {

    /**
     * A regular expression pattern that matches any sequence of characters that are
     * not Unicode letters. This is used to normalize word boundaries.
     */
    private static final Pattern BOUNDARIES = Pattern.compile("[^\\p{L}]+");

    /**
     * Normalizes the input text by:
     * <ul>
     *     <li>Converting all characters to lowercase</li>
     *     <li>Replacing non-letter sequences with single spaces</li>
     *     <li>Trimming leading and trailing whitespace</li>
     * </ul>
     *
     * This method is commonly used to prepare strings for profanity matching,
     * ensuring consistent formatting across different languages and inputs.
     *
     * @param text the original input text
     * @return a normalized version of the text
     */
    public static String normalize(String text) {
        text = text.toLowerCase();
        text = BOUNDARIES.matcher(text).replaceAll(" ");
        text = text.trim();

        return text;
    }
}


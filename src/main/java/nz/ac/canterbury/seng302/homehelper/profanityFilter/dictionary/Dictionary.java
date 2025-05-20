package nz.ac.canterbury.seng302.homehelper.profanityFilter.dictionary;


import nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus.UnixLineReader;
import nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus.UnixLineWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Dictionary class represents a language-specific collection of profane words
 * along with their severity scores. It provides functionality to read profanity entries
 * from a file, match input strings against known profanities, and write the dictionary to an output.
 */
public class Dictionary implements Iterable<Profanity> {

    /**
     * Interface for matching input strings against profane words.
     */
    public interface Matcher {
        /**
         * Finds the first profane word in the input string.
         *
         * @param input the input text to search
         * @return a {@link Profanity} instance if found; {@code null} otherwise
         */
        Profanity find(String input);

        /**
         * Checks whether the input string contains any profanity.
         *
         * @param input the input text to test
         * @return {@code true} if a match is found; {@code false} otherwise
         */
        boolean matches(String input);
    }

    /**
     * Matcher implementation that never matches any input (used as a fallback).
     */
    private static class FalseMatcher implements Matcher {
        public static final FalseMatcher INSTANCE = new FalseMatcher();

        @Override
        public Profanity find(String input) {
            return null;
        }

        @Override
        public boolean matches(String input) {
            return false;
        }
    }

    /**
     * Matcher implementation using a regular expression to detect profanity.
     */
    private class RegexMatcher implements Matcher {
        private final Pattern regex;

        RegexMatcher(Pattern regex) {
            this.regex = regex;
        }

        @Override
        public Profanity find(String input) {
            String strProfanity = findString(input);
            return strProfanity == null ? null : Dictionary.this.profanities.get(strProfanity);
        }

        @Override
        public boolean matches(String input) {
            return findString(input) != null;
        }

        /**
         * Finds the matched string using regex.
         *
         * @param input the normalized input
         * @return the matched profanity string or {@code null}
         */
        private String findString(String input) {
            input = ' ' + Text.normalize(input) + ' ';
            java.util.regex.Matcher matcher = regex.matcher(input);
            return matcher.find() ? matcher.group(1) : null;
        }
    }

    private class MultiRegexMatcher implements Matcher {
        private final List<Pattern> patterns;

        public MultiRegexMatcher(List<Pattern> patterns) {
            this.patterns = patterns;
        }

        @Override
        public Profanity find(String input) {
            input = input.toLowerCase();
            List <String> inputs = Arrays.stream(input.split(" ")).toList();
            for (Pattern pattern : patterns) {
                for (String word : inputs) {
                    if (pattern.matcher(word).matches()) {
                        return new Profanity(pattern.toString(), 1.0f); // or look up actual score if needed
                    }
                }
            }
            return null;
        }

        @Override
        public boolean matches(String input) {
            return find(input) != null;
        }
    }

    private final Map<String, Profanity> profanities;
    private final boolean isSpaceSeparated;

    /**
     * Constructs a {@code Dictionary} for the given language and set of profanities.
     *
     * @param language    the language code ("en" - english)
     * @param profanities the set of profane entries
     */
    public Dictionary(String language, Set<Profanity> profanities) {
        this.profanities = new HashMap<>(profanities.size());
        for (Profanity profanity : profanities)
            this.profanities.put(profanity.text(), profanity);
        this.isSpaceSeparated = !("zh".equals(language) || "ja".equals(language) || "th".equals(language));
    }

    /**
     * Reads a {@code Dictionary} from a file.
     *
     * @param language the language code
     * @param file     the file containing profanity entries
     * @return the constructed {@code Dictionary}
     * @throws IOException if reading fails
     */
    public static Dictionary read(String language, File file) throws IOException {
        try (InputStream stream = new FileInputStream(file)) {
            return read(language, stream);
        }
    }

    /**
     * Reads a {@code Dictionary} from an input stream.
     *
     * @param language the language code
     * @param stream   the input stream containing profanity entries
     * @return the constructed {@code Dictionary}
     * @throws IOException if reading fails
     */
    public static Dictionary read(String language, InputStream stream) throws IOException {
        HashSet<Profanity> profanities = new HashSet<>();
        UnixLineReader reader = new UnixLineReader(stream, StandardCharsets.UTF_8);

        String line;
        while ((line = reader.readLine()) != null) {
            int idx = line.indexOf('#');
            if (idx >= 0) line = line.substring(0, idx);
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] cols = line.split("\t");
            String text = cols[0];
            if (text.isEmpty())
                throw new IOException("Invalid value at line: \"" + line + "\"");
            float score = cols.length > 1 ? Float.parseFloat(cols[1]) : 1.f;
            profanities.add(new Profanity(text, score));
        }

        return new Dictionary(language, profanities);
    }

    /**
     * Writes the {@code Dictionary} to a file.
     *
     * @param file the file to write to
     * @throws IOException if writing fails
     */
    public void write(File file) throws IOException {
        try (OutputStream stream = new FileOutputStream(file)) {
            write(stream);
        }
    }

    /**
     * Writes the {@code Dictionary} to an output stream.
     *
     * @param stream the output stream to write to
     * @throws IOException if writing fails
     */
    public void write(OutputStream stream) throws IOException {
        UnixLineWriter writer = new UnixLineWriter(stream, StandardCharsets.UTF_8);
        List<Profanity> entries = new ArrayList<>(profanities.values());
        Collections.sort(entries);
        Collections.reverse(entries);

        for (Profanity profanity : entries) {
            writer.writeLine(profanity.toString());
        }

        writer.flush();
    }

    /**
     * Returns a {@link Matcher} that can be used to search for profanities in text
     * with a severity above the given threshold.
     *
     * @param threshold the minimum severity score to consider
     * @return a {@link Matcher} instance
     */
    public Matcher matcher(float threshold) {
        List<Pattern> regexPatterns = new ArrayList<>();

        for (Profanity profanity : profanities.values()) {
            if (profanity.score() >= threshold) {
                try {
                    Pattern pattern = Pattern.compile(profanity.text(), Pattern.CASE_INSENSITIVE);
                    regexPatterns.add(pattern);
                } catch (Exception e) {
                    System.err.println("Invalid regex in dictionary: " + profanity.text());
                }
            }
        }

        if (regexPatterns.isEmpty()) return FalseMatcher.INSTANCE;

        return new MultiRegexMatcher(regexPatterns);
    }

    /**
     * Returns an iterator over the {@link Profanity} entries in this dictionary.
     *
     * @return an iterator of {@code Profanity} objects
     */
    @Override
    public Iterator<Profanity> iterator() {
        return profanities.values().iterator();
    }
}
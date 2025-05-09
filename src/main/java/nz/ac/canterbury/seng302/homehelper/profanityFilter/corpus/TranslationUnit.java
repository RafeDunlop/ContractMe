package nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus;

/**
 * Represents a single translation unit in a parallel corpus,
 * consisting of a source sentence and its corresponding translation.
 */
public class TranslationUnit {

    private final String sentence;
    private final String translation;

    /**
     * Constructs a {@code TranslationUnit} with the given source sentence and translation.
     *
     * @param sentence    the original sentence from the source file
     * @param translation the translated sentence from the target file
     */
    public TranslationUnit(String sentence, String translation) {
        this.sentence = sentence;
        this.translation = translation;
    }

    /**
     * Returns the source sentence of this translation unit.
     *
     * @return the source sentence
     */
    public String sentence() {
        return sentence;
    }

    /**
     * Returns the translated sentence of this translation unit.
     *
     * @return the translation
     */
    public String translation() {
        return translation;
    }
}


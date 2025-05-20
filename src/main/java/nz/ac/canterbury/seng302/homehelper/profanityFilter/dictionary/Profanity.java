package nz.ac.canterbury.seng302.homehelper.profanityFilter.dictionary;

import java.util.Objects;

/**
 * The {@code Profanity} class represents a single profane word or phrase along with an associated severity score.
 * This score is used to evaluate the likelihood or intensity of the word being considered offensive.
 * Instances of {@code Profanity} are immutable and comparable based on their severity score.
 */
public class Profanity implements Comparable<Profanity> {

    /**
     * The profane word or phrase.
     */
    private final String text;

    /**
     * The severity score of the profanity, in the range [0.0, 1.0].
     */
    private final float score;

    /**
     * Constructs a new {@code Profanity} object with the specified text and score.
     *
     * @param text  the profane word or phrase
     * @param score the severity score associated with the profanity
     */
    public Profanity(String text, float score) {
        this.text = text;
        this.score = score;
    }

    /**
     * Returns the profane word or phrase.
     *
     * @return the profanity text
     */
    public String text() {
        return text;
    }

    /**
     * Returns the severity score associated with this profanity.
     *
     * @return the score of the profanity
     */
    public float score() {
        return score;
    }

    /**
     * Compares this profanity to another based on score.
     *
     * @param o the other {@code Profanity} to compare to
     * @return a negative integer, zero, or a positive integer as this object's score
     *         is less than, equal to, or greater than the specified object's score
     */
    @Override
    public int compareTo(Profanity o) {
        return Float.compare(score, o.score);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * Two {@code Profanity} objects are considered equal if they have the same text.
     *
     * @param o the reference object with which to compare
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profanity profanity = (Profanity) o;
        return text.equals(profanity.text);
    }

    /**
     * Returns a hash code value for the object, based on the text.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    /**
     * Returns a string representation of the profanity, in the format:
     * {@code text \t score}
     *
     * @return the string representation of this profanity
     */
    @Override
    public String toString() {
        return text + '\t' + score;
    }
}

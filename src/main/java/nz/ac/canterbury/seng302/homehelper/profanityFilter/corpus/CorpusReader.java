package nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Reads translation units from a {@link Corpus} by reading corresponding lines
 * from the source and target files in parallel.
 * <p>
 * Implements {@link Closeable} to ensure resources are released after use.
 */
public class CorpusReader implements Closeable {

    private final UnixLineReader sourceReader;
    private final UnixLineReader targetReader;

    /**
     * Constructs a {@code CorpusReader} for the given {@link Corpus}.
     *
     * @param corpus the corpus containing the source and target files
     * @throws IOException if an I/O error occurs while opening the files
     */
    public CorpusReader(Corpus corpus) throws IOException {
        boolean success = false;

        try {
            this.sourceReader = new UnixLineReader(new FileInputStream(corpus.source()), StandardCharsets.UTF_8);
            this.targetReader = new UnixLineReader(new FileInputStream(corpus.target()), StandardCharsets.UTF_8);
            success = true;
        } finally {
            if (!success)
                this.close();
        }
    }

    /**
     * Reads the next {@link TranslationUnit} from the corpus.
     * Reads one line from each of the source and target files.
     *
     * @return the next translation unit, or {@code null} if end of both files is reached
     * @throws IOException if the lines are unmatched or another I/O error occurs
     */
    public TranslationUnit read() throws IOException {
        String source = sourceReader.readLine();
        String target = targetReader.readLine();

        if (source == null && target == null) {
            return null;
        } else if (source != null && target != null) {
            return new TranslationUnit(source, target);
        } else {
            throw new IOException("Invalid parallel corpus, unmatched line");
        }
    }

    /**
     * Closes the source and target file readers.
     * If both readers throw exceptions, only the first is propagated.
     *
     * @throws IOException if an I/O error occurs while closing either reader
     */
    @Override
    public void close() throws IOException {
        IOException ioe = null;

        try {
            if (this.sourceReader != null)
                this.sourceReader.close();
        } catch (IOException e) {
            ioe = e;
        }

        try {
            if (this.targetReader != null)
                this.targetReader.close();
        } catch (IOException e) {
            ioe = e;
        }

        if (ioe != null)
            throw ioe;
    }
}

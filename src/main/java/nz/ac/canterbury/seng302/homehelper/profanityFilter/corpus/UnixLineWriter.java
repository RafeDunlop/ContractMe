package nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by davide on 04/07/16.
 * A writer that outputs text lines using Unix-style line endings (`\n`) and
 * ensures consistency in line formatting by replacing embedded newlines.
 * <p>
 * Designed for use with parallel corpora or any text output where line-based
 * structure is important.
 */
public class UnixLineWriter implements Closeable {

    protected final Writer writer;

    /**
     * Constructs a {@code UnixLineWriter} that writes to the specified file
     * using the given character encoding.
     *
     * @param file    the output file
     * @param charset the charset to encode characters
     * @throws FileNotFoundException if the file cannot be created or opened
     */
    public UnixLineWriter(File file, Charset charset) throws FileNotFoundException {
        this(new FileOutputStream(file), charset);
    }

    /**
     * Constructs a {@code UnixLineWriter} from an output stream and charset.
     *
     * @param stream  the output stream to write to
     * @param charset the character encoding to use
     */
    public UnixLineWriter(OutputStream stream, Charset charset) {
        this(new OutputStreamWriter(stream, charset));
    }

    /**
     * Constructs a {@code UnixLineWriter} with a given {@link Writer}.
     *
     * @param writer the writer to wrap
     */
    public UnixLineWriter(Writer writer) {
        this.writer = writer;
    }

    /**
     * Flushes the underlying writer to ensure all buffered data is written.
     *
     * @throws IOException if an I/O error occurs
     */
    public void flush() throws IOException {
        this.writer.flush();
    }

    /**
     * Writes a single line to the output, replacing any embedded newline characters
     * with spaces, and appends a Unix-style line ending (`\n`).
     *
     * @param line the line of text to write
     * @throws IOException if an I/O error occurs during writing
     */
    public void writeLine(String line) throws IOException {
        this.writer.write(line.replace('\n', ' '));
        this.writer.write('\n');
    }

    /**
     * Flushes and closes the underlying writer, releasing system resources.
     *
     * @throws IOException if an I/O error occurs during closing
     */
    @Override
    public void close() throws IOException {
        this.writer.flush();
        this.writer.close();
    }
}
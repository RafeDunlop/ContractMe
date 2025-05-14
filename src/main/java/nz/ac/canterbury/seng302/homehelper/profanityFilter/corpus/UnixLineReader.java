package nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by davide on 24/02/16.
 * A line reader designed to handle Unix-style line endings (`\n`) explicitly.
 * <p>
 * This reader supports reading from files or streams with the ability to manage
 * buffer states and preserve consistent behavior across different character encodings.
 * Unlike {@link BufferedReader}, this reader offers more control over how lines are terminated.
 */
public class UnixLineReader implements Closeable {

    private final Reader reader;
    private final char[] buffer;
    private int nextChar = 0;
    private int bufferLen = 0;

    private static final int defaultCharBufferSize = 8192;
    private static final int defaultExpectedLineLength = 80;

    /**
     * Constructs a {@code UnixLineReader} from a {@link File} and character encoding.
     *
     * @param file    the file to read from
     * @param charset the character set used to decode bytes into characters
     * @throws FileNotFoundException if the file does not exist or cannot be opened
     */
    public UnixLineReader(File file, Charset charset) throws FileNotFoundException {
        this(new FileInputStream(file), charset);
    }

    /**
     * Constructs a {@code UnixLineReader} from an {@link InputStream} and character encoding.
     *
     * @param stream  the input stream to read from
     * @param charset the character set used to decode bytes into characters
     */
    public UnixLineReader(InputStream stream, Charset charset) {
        this(new InputStreamReader(stream, charset));
    }

    /**
     * Constructs a {@code UnixLineReader} from a {@link Reader}.
     *
     * @param reader the reader to wrap
     */
    public UnixLineReader(Reader reader) {
        this.reader = reader;
        this.buffer = new char[defaultCharBufferSize];
    }

    /**
     * Reads a single line from the input, handling Unix (`\n`) and optional Windows (`\r\n`) line endings.
     *
     * @return the next line of text, or {@code null} if end of input is reached
     * @throws IOException if an I/O error occurs while reading
     */
    public String readLine() throws IOException {
        if (bufferLen < 0)
            return null;

        StringBuffer s = new StringBuffer(defaultExpectedLineLength);

        for (; ; ) {
            boolean stop = fillFromBuffer(s);
            if (stop) break;

            bufferLen = reader.read(buffer, 0, buffer.length);
            nextChar = 0;
            if (bufferLen < 0)
                return s.length() > 0 ? s.toString() : null;
        }

        return s.toString();
    }

    /**
     * Reads characters from the internal buffer into the provided {@code StringBuffer}
     * until a line terminator (`\n`) is found or the buffer ends.
     *
     * @param s the buffer to append characters to
     * @return {@code true} if a full line was read, {@code false} if the buffer needs to be refilled
     */
    private boolean fillFromBuffer(StringBuffer s) {
        boolean stop = false;
        int offset = nextChar;
        int len = 0;

        boolean lastWasCarriageReturn = false;

        for (; nextChar < bufferLen; nextChar++) {
            if (buffer[nextChar] == '\n') {
                stop = true;
                nextChar++;
                if (lastWasCarriageReturn) len--;
                break;
            } else {
                lastWasCarriageReturn = buffer[nextChar] == '\r';
                len++;
            }
        }

        if (len > 0)
            s.append(buffer, offset, len);

        return stop;
    }

    /**
     * Closes the underlying reader and releases associated system resources.
     *
     * @throws IOException if an I/O error occurs while closing the reader
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }
}
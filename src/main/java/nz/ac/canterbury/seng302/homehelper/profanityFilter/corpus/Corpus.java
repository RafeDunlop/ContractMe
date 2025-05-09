package nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a parallel corpus with associated source and target files.
 * Used for managing corpora in the profanity filter module.
 */
public class Corpus {

    private final File source;
    private final File target;

    /**
     * Constructs a Corpus with the specified source and target files.
     *
     * @param source the source language file
     * @param target the target language file
     */
    public Corpus(File source, File target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Returns the source file of this corpus.
     *
     * @return the source file
     */
    public File source() {
        return source;
    }

    /**
     * Returns the target file of this corpus.
     *
     * @return the target file
     */
    public File target() {
        return target;
    }

    /**
     * Returns a file with the same name (without extension) as the given file,
     * but with a new extension.
     *
     * @param file the base file
     * @param extension the new extension to apply
     * @return the new file with the given extension
     */
    private static File fileWithExtension(File file, String extension) {
        String name = file.getName();
        int ld = name.lastIndexOf('.');
        if (ld >= 0)
            name = name.substring(0, ld);

        return new File(file.getParentFile(), name + "." + extension);
    }

    /**
     * Lists all valid Corpus pairs from a given folder, matching source and target file extensions.
     *
     * @param source the file extension for source files (without the dot)
     * @param target the file extension for target files (without the dot)
     * @param folder the folder to scan for corpus files
     * @return a list of valid Corpus objects
     * @throws FileNotFoundException if the provided folder is not a directory
     */
    public static List<Corpus> list(String source, String target, File folder) throws FileNotFoundException {
        if (!folder.isDirectory())
            throw new FileNotFoundException(folder.toString());

        File[] sources = folder.listFiles((dir, name) -> name.endsWith("." + source));
        if (sources == null)
            return Collections.emptyList();

        ArrayList<Corpus> corpora = new ArrayList<>(sources.length);
        for (File sourceFile : sources) {
            File targetFile = fileWithExtension(sourceFile, target);
            if (targetFile.isFile())
                corpora.add(new Corpus(sourceFile, targetFile));
        }
        return corpora;
    }
}

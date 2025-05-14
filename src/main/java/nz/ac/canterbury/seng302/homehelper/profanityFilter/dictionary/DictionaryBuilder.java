package nz.ac.canterbury.seng302.homehelper.profanityFilter.dictionary;

import nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus.Corpus;
import nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus.CorpusReader;
import nz.ac.canterbury.seng302.homehelper.profanityFilter.corpus.TranslationUnit;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The {@code DictionaryBuilder} class is responsible for creating a new {@link Dictionary}
 * by analyzing a set of parallel corpora. It estimates profanity scores based on
 * co-occurrence frequencies between source and translated sentences.
 * This builder uses multithreaded processing to efficiently compute profanity statistics.
 */
public class DictionaryBuilder {

    private final String language;
    private final int threads;

    /**
     * Constructs a {@code DictionaryBuilder} using the number of available processors as thread count.
     *
     * @param language the language code for the dictionary ("en" - english)
     */
    public DictionaryBuilder(String language) {
        this(language, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Constructs a {@code DictionaryBuilder} with a specified thread count.
     *
     * @param language the language code for the dictionary
     * @param threads  the number of threads to use for parallel processing
     */
    public DictionaryBuilder(String language, int threads) {
        this.language = language;
        this.threads = threads;
    }

    /**
     * Builds a new {@link Dictionary} by processing the given list of corpora.
     * It compares the frequency of profanities in source sentences with their translations
     * to compute a severity score for each profanity.
     *
     * @param corpora   the list of {@link Corpus} instances to process
     * @param input     the input dictionary containing initial profanities
     * @param reference a reference dictionary for the translated content
     * @return a new {@link Dictionary} with updated profanity scores
     * @throws IOException          if a corpus file cannot be read
     * @throws InterruptedException if the processing is interrupted
     */
    public Dictionary build(List<Corpus> corpora, Dictionary input, Dictionary reference)
            throws IOException, InterruptedException {

        FixedThreadsExecutor executor = new FixedThreadsExecutor(threads);

        Dictionary.Matcher sentenceMatcher = input.matcher(0.f);
        Dictionary.Matcher translationMatcher = reference.matcher(0.f);

        Map<Profanity, Counter> table = new HashMap<>();
        for (Profanity profanity : input)
            table.put(profanity, new Counter(profanity));

        try {
            for (Corpus corpus : corpora) {
                try (CorpusReader reader = new CorpusReader(corpus)) {
                    TranslationUnit _tu;
                    while ((_tu = reader.read()) != null) {
                        TranslationUnit tu = _tu;

                        executor.submit(() -> {
                            Profanity profanity = sentenceMatcher.find(tu.sentence());
                            if (profanity != null) {
                                Counter counter = table.get(profanity);
                                counter.frequency.incrementAndGet();

                                if (translationMatcher.matches(tu.translation()))
                                    counter.cooccurrences.incrementAndGet();
                            }
                        });
                    }
                }
            }

            executor.shutdown();
            if (!executor.awaitTermination(1L, TimeUnit.DAYS)) {
                throw new InterruptedException("Timeout waiting for tasks to complete.");
            }
        } finally {
            executor.shutdownNow();
        }

        return createDictionary(table);
    }

    /**
     * Creates a new {@link Dictionary} from the computed profanity frequencies.
     *
     * @param table the map of profanities and their usage statistics
     * @return a {@code Dictionary} with updated scores
     */
    private Dictionary createDictionary(Map<Profanity, Counter> table) {
        Set<Profanity> profanities = new HashSet<>(table.size());

        for (Counter counter : table.values()) {
            String text = counter.profanity.text();
            int totalFreq = (int) counter.frequency.get();
            int profanityFreq = (int) counter.cooccurrences.get();
            double score = totalFreq > 0 ? ((float) profanityFreq / totalFreq) : 0.;

            profanities.add(new Profanity(text, (float) score));
        }

        return new Dictionary(language, profanities);
    }

    /**
     * Internal class used to count occurrences and co-occurrences of profanities.
     */
    private static class Counter {
        public final Profanity profanity;
        public AtomicLong frequency = new AtomicLong(0L);
        public AtomicLong cooccurrences = new AtomicLong(0L);

        public Counter(Profanity profanity) {
            this.profanity = profanity;
        }
    }

    /**
     * A simple thread pool with a fixed number of concurrent tasks and throttling using a semaphore.
     */
    private static class FixedThreadsExecutor {

        private final ExecutorService executor;
        private final Semaphore permits;

        /**
         * Constructs a {@code FixedThreadsExecutor} with the specified number of threads.
         *
         * @param threads the number of threads
         */
        public FixedThreadsExecutor(int threads) {
            executor = Executors.newFixedThreadPool(threads);
            permits = new Semaphore(threads * 4); // Limit concurrent tasks
        }

        /**
         * Submits a task to be executed, blocking if too many tasks are pending.
         *
         * @param task the task to execute
         */
        public void submit(Runnable task) {
            try {
                permits.acquire();
            } catch (InterruptedException e) {
                throw new RejectedExecutionException(e);
            }

            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    permits.release();
                }
            });
        }

        /**
         * Initiates an orderly shutdown.
         */
        public void shutdown() {
            executor.shutdown();
        }

        /**
         * Awaits termination of all tasks or times out.
         *
         * @param timeout the timeout duration
         * @param unit    the time unit
         * @return {@code true} if terminated normally; {@code false} if timed out
         * @throws InterruptedException if interrupted while waiting
         */
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return executor.awaitTermination(timeout, unit);
        }

        /**
         * Immediately stops all actively executing tasks.
         */
        public void shutdownNow() {
            executor.shutdownNow();
        }
    }

    /**
     * Extracts the file extension from a file.
     *
     * @param file the file object
     * @return the file extension, or {@code null} if not present
     */
    private static String extension(File file) {
        String name = file.getName();
        int idx = name.lastIndexOf('.');
        return idx < 0 ? null : name.substring(idx + 1);
    }
}

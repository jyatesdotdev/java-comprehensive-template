package com.example.template.etl.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Generic, composable ETL pipeline using functional interfaces.
 *
 * <p>This is a pure-Java pipeline abstraction (no framework dependency) useful for
 * in-process data transformations, testing pipeline logic, or lightweight ETL where
 * Spark/Batch overhead is unnecessary.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * var result = DataPipeline.<RawRecord>extract(() -> readFromSource())
 *     .filter(r -> r.isValid())
 *     .transform(r -> enrich(r))
 *     .transform(r -> normalize(r))
 *     .load(batch -> writeToDB(batch), 500);
 * }</pre>
 *
 * @param <T> current element type in the pipeline
 */
public class DataPipeline<T> {

    private final Iterable<T> source;

    private DataPipeline(Iterable<T> source) {
        this.source = source;
    }

    /**
     * Begin a pipeline from an extraction source.
     *
     * @param extractor supplies the raw data to process
     * @param <T>       element type produced by the extractor
     * @return a new pipeline over the extracted data
     */
    public static <T> DataPipeline<T> extract(Extractor<T> extractor) {
        return new DataPipeline<>(extractor.extract());
    }

    /**
     * Begin a pipeline from an existing collection.
     *
     * @param data the elements to process
     * @param <T>  element type
     * @return a new pipeline over the supplied data
     */
    public static <T> DataPipeline<T> of(Iterable<T> data) {
        return new DataPipeline<>(data);
    }

    /**
     * Filter elements, retaining only those matching the predicate.
     *
     * @param predicate test applied to each element
     * @return a new pipeline containing only matching elements
     */
    public DataPipeline<T> filter(Predicate<T> predicate) {
        List<T> filtered = new ArrayList<>();
        for (T item : source) {
            if (predicate.test(item)) filtered.add(item);
        }
        return new DataPipeline<>(filtered);
    }

    /**
     * Transform each element by applying a mapping function.
     *
     * @param mapper function applied to each element
     * @param <R>    result element type
     * @return a new pipeline of transformed elements
     */
    public <R> DataPipeline<R> transform(Function<T, R> mapper) {
        List<R> result = new ArrayList<>();
        for (T item : source) {
            result.add(mapper.apply(item));
        }
        return new DataPipeline<>(result);
    }

    /**
     * Load all elements by passing them to the loader in batches.
     *
     * @param loader    receives each batch of elements
     * @param batchSize maximum number of elements per batch
     * @return total number of elements loaded
     */
    public int load(Loader<T> loader, int batchSize) {
        List<T> batch = new ArrayList<>(batchSize);
        int total = 0;
        for (T item : source) {
            batch.add(item);
            if (batch.size() >= batchSize) {
                loader.load(batch);
                total += batch.size();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            loader.load(batch);
            total += batch.size();
        }
        return total;
    }

    /**
     * Collect all pipeline results into a list.
     *
     * @return list of all elements in the pipeline
     */
    public List<T> collect() {
        List<T> result = new ArrayList<>();
        source.forEach(result::add);
        return result;
    }

    /**
     * Extraction source — produces an iterable of raw data.
     *
     * @param <T> element type produced by extraction
     */
    @FunctionalInterface
    public interface Extractor<T> {
        /**
         * Extract data from the source.
         *
         * @return iterable of extracted elements
         */
        Iterable<T> extract();
    }

    /**
     * Batch loader — receives chunks of processed data.
     *
     * @param <T> element type consumed by the loader
     */
    @FunctionalInterface
    public interface Loader<T> {
        /**
         * Load a batch of elements to the destination.
         *
         * @param batch collection of elements to load
         */
        void load(Collection<T> batch);
    }
}

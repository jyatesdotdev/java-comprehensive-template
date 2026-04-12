package com.example.template.etl.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch job configuration: CSV → transform → JSON.
 *
 * <p>Demonstrates the core Spring Batch 5 patterns:</p>
 * <ul>
 *   <li>Chunk-oriented processing with configurable chunk size</li>
 *   <li>{@link FlatFileItemReader} for delimited file input</li>
 *   <li>{@link ItemProcessor} for business transformation</li>
 *   <li>{@link JsonFileItemWriter} for structured output</li>
 *   <li>Automatic retry/skip policies (see step configuration)</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>
 * &#64;SpringBootApplication
 * &#64;EnableBatchProcessing
 * public class BatchApp { public static void main(String[] args) {
     SpringApplication.run(BatchApp.class, args);
 } }
 * </pre>
 */
@Configuration
public class CsvToJsonBatchConfig {

    private static final Logger log = LoggerFactory.getLogger(CsvToJsonBatchConfig.class);
    private static final int CHUNK_SIZE = 100;

    /**
     * Input record mapped from CSV columns.
     *
     * @param id       unique record identifier
     * @param name     display name
     * @param amount   raw amount as a string (parsed during processing)
     * @param category classification category
     */
    public record InputRecord(String id, String name, String amount, String category) {}

    /**
     * Output record after transformation.
     *
     * @param id        unique record identifier
     * @param name      display name
     * @param amountUsd parsed amount in USD
     * @param category  classification category
     * @param tier      computed tier (STANDARD, BUSINESS, or ENTERPRISE)
     */
    public record OutputRecord(String id, String name, double amountUsd, String category, String tier) {}

    /**
     * Reads CSV rows from {@code data/input.csv} on the classpath, skipping the header line.
     *
     * @return configured CSV item reader
     */
    @Bean
    public FlatFileItemReader<InputRecord> csvReader() {
        return new FlatFileItemReaderBuilder<InputRecord>()
                .name("csvReader")
                .resource(new ClassPathResource("data/input.csv"))
                .delimited()
                .names("id", "name", "amount", "category")
                .targetType(InputRecord.class)
                .linesToSkip(1) // skip header
                .build();
    }

    /**
     * Transforms an {@link InputRecord} into an {@link OutputRecord} by parsing the amount,
     * filtering non-positive values, and computing a tier classification.
     *
     * @return processor that converts input records to output records (or {@code null} to skip)
     */
    @Bean
    public ItemProcessor<InputRecord, OutputRecord> transformProcessor() {
        return input -> {
            double amount = Double.parseDouble(input.amount());
            if (amount <= 0) {
                log.debug("Filtering out record with non-positive amount: {}", input.id());
                return null; // returning null filters the item
            }
            String tier = amount >= 10_000 ? "ENTERPRISE" : amount >= 1_000 ? "BUSINESS" : "STANDARD";
            return new OutputRecord(input.id(), input.name(), amount, input.category(), tier);
        };
    }

    /**
     * Writes {@link OutputRecord} instances as JSON to {@code output/results.json}.
     *
     * @return configured JSON item writer
     */
    @Bean
    public JsonFileItemWriter<OutputRecord> jsonWriter() {
        return new JsonFileItemWriterBuilder<OutputRecord>()
                .name("jsonWriter")
                .resource(new FileSystemResource("output/results.json"))
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .build();
    }

    /**
     * Defines the chunk-oriented step that reads, processes, and writes records.
     * Configured with fault tolerance to skip up to 10 {@link NumberFormatException}s.
     *
     * @param jobRepository job metadata repository
     * @param txManager     transaction manager for chunk commits
     * @param reader        CSV item reader
     * @param processor     transformation processor
     * @param writer        JSON item writer
     * @return configured batch step
     */
    @Bean
    public Step csvToJsonStep(JobRepository jobRepository,
                              PlatformTransactionManager txManager,
                              ItemReader<InputRecord> reader,
                              ItemProcessor<InputRecord, OutputRecord> processor,
                              ItemWriter<OutputRecord> writer) {
        return new StepBuilder("csvToJsonStep", jobRepository)
                .<InputRecord, OutputRecord>chunk(CHUNK_SIZE, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(NumberFormatException.class) // skip malformed amounts
                .build();
    }

    /**
     * Defines the batch job composed of the CSV-to-JSON step.
     *
     * @param jobRepository job metadata repository
     * @param csvToJsonStep the processing step
     * @return configured batch job
     */
    @Bean
    public Job csvToJsonJob(JobRepository jobRepository, Step csvToJsonStep) {
        return new JobBuilder("csvToJsonJob", jobRepository)
                .start(csvToJsonStep)
                .build();
    }
}

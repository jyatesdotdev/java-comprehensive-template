package com.example.template.etl.spark;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FilterFunction;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.MapGroupsFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import static org.apache.spark.sql.functions.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Apache Spark ETL examples demonstrating both RDD and Dataset/DataFrame APIs.
 *
 * <p>Spark runs in local mode here for demonstration. In production, submit
 * jobs via {@code spark-submit} with cluster manager configuration.</p>
 *
 * <h3>Running</h3>
 * <pre>
 * // Local mode (development)
 * SparkEtlExample.wordCountRdd(List.of("hello world", "hello spark"));
 *
 * // Production: spark-submit --master yarn --class com.example.template.etl.spark.SparkEtlExample app.jar
 * </pre>
 */
public final class SparkEtlExample {

    private SparkEtlExample() {}

    /**
     * Classic word count using the RDD API.
     *
     * <p>Pattern: Extract (parallelize) → Transform (flatMap/mapToPair/reduceByKey) → Collect</p>
     *
     * @param lines input text lines to count words from
     * @return list of (word, count) tuples
     */
    public static List<scala.Tuple2<String, Integer>> wordCountRdd(List<String> lines) {
        SparkConf conf = new SparkConf()
                .setAppName("WordCount-RDD")
                .setMaster("local[*]");

        try (JavaSparkContext sc = new JavaSparkContext(conf)) {
            JavaRDD<String> rdd = sc.parallelize(lines);

            return rdd
                    .flatMap(line -> Arrays.asList(line.toLowerCase().split("\\W+")).iterator())
                    .filter(word -> !word.isEmpty())
                    .mapToPair(word -> new scala.Tuple2<>(word, 1))
                    .reduceByKey(Integer::sum)
                    .collect();
        }
    }

    /**
     * Structured ETL using the Dataset/DataFrame API (preferred for Spark 3.x).
     *
     * <p>Demonstrates reading CSV, transforming with SQL expressions, and writing output.
     * The DataFrame API provides schema enforcement, catalyst optimization, and SQL interop.</p>
     *
     * @param inputPath  path to the input CSV file
     * @param outputPath path for Parquet output, or {@code null} to skip writing
     * @return transformed and aggregated dataset
     */
    public static Dataset<Row> csvTransformExample(String inputPath, String outputPath) {
        SparkSession spark = SparkSession.builder()
                .appName("CSV-ETL")
                .master("local[*]")
                .getOrCreate();

        try {
            // Extract: read CSV with inferred schema
            Dataset<Row> raw = spark.read()
                    .option("header", "true")
                    .option("inferSchema", "true")
                    .csv(inputPath);

            // Transform: filter, derive columns, aggregate
            Dataset<Row> transformed = raw
                    .filter(col("amount").gt(0))
                    .withColumn("amount_usd", col("amount").multiply(col("exchange_rate")))
                    .groupBy("category")
                    .agg(
                            sum("amount_usd").alias("total_usd"),
                            count("*").alias("transaction_count")
                    );

            // Load: write as Parquet (columnar, compressed)
            if (outputPath != null) {
                transformed.write()
                        .mode("overwrite")
                        .parquet(outputPath);
            }

            return transformed;
        } finally {
            spark.stop();
        }
    }

    /**
     * Aggregates completed transactions by category using Spark SQL.
     *
     * @param spark active Spark session
     * @param input dataset to register as the {@code transactions} temp view
     * @return aggregated dataset with count, total, average, and p95 per category
     */
    public static Dataset<Row> sparkSqlExample(SparkSession spark, Dataset<Row> input) {
        input.createOrReplaceTempView("transactions");

        return spark.sql("""
                SELECT category,
                       COUNT(*)           AS cnt,
                       SUM(amount)        AS total,
                       AVG(amount)        AS avg_amount,
                       PERCENTILE(amount, 0.95) AS p95
                FROM   transactions
                WHERE  status = 'COMPLETED'
                GROUP  BY category
                HAVING COUNT(*) > 10
                ORDER  BY total DESC
                """);
    }

    /**
     * Simple record for typed Dataset operations.
     *
     * @param product product name
     * @param region  sales region
     * @param revenue revenue amount
     */
    public record SalesRecord(String product, String region, double revenue) implements Serializable {
        @java.io.Serial
        private static final long serialVersionUID = 1L;
    }

    /**
     * Typed Dataset API — compile-time type safety with Spark optimizations.
     *
     * @param spark active Spark session
     * @param data  sales records to process
     * @return dataset of aggregated average revenue per region
     */
    public static Dataset<SalesRecord> typedDatasetExample(SparkSession spark, List<SalesRecord> data) {
        Dataset<SalesRecord> ds = spark.createDataset(data, Encoders.bean(SalesRecord.class));

        return ds.filter((FilterFunction<SalesRecord>) r -> r.revenue() > 100.0)
                .groupByKey((MapFunction<SalesRecord, String>) SalesRecord::region, Encoders.STRING())
                .mapGroups((MapGroupsFunction<String, SalesRecord, SalesRecord>) (region, iter) -> {
                    double total = 0;
                    int count = 0;
                    while (iter.hasNext()) {
                        total += iter.next().revenue();
                        count++;
                    }
                    return new SalesRecord("AGGREGATED", region, total / count);
                }, Encoders.bean(SalesRecord.class));
    }
}

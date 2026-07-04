package com.example.template.etl.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DataPipeline} covering extract, filter, transform, load, and collect. */
class DataPipelineTest {

  @Test
  void filterAndTransform() {
    List<Integer> result =
        DataPipeline.of(List.of(1, 2, 3, 4, 5))
            .filter(n -> n % 2 == 0)
            .transform(n -> n * 10)
            .collect();

    assertThat(result).containsExactly(20, 40);
  }

  @Test
  void loadInBatches() {
    List<Collection<String>> batches = new ArrayList<>();

    int total =
        DataPipeline.of(List.of("a", "b", "c", "d", "e"))
            .load(batch -> batches.add(new ArrayList<>(batch)), 2);

    assertThat(total).isEqualTo(5);
    assertThat(batches).hasSize(3); // [a,b], [c,d], [e]
    assertThat(batches.get(0)).containsExactly("a", "b");
    assertThat(batches.get(1)).containsExactly("c", "d");
    assertThat(batches.get(2)).containsExactly("e");
  }

  @Test
  void extractFilterTransformLoad() {
    record Sale(String product, double amount) {}

    List<Collection<String>> loaded = new ArrayList<>();

    int count =
        DataPipeline.<Sale>extract(
                () -> List.of(new Sale("A", 100), new Sale("B", 50), new Sale("C", 200)))
            .filter(s -> s.amount() >= 100)
            .transform(s -> s.product() + ":" + s.amount())
            .load(batch -> loaded.add(new ArrayList<>(batch)), 10);

    assertThat(count).isEqualTo(2);
    assertThat(loaded.getFirst()).containsExactly("A:100.0", "C:200.0");
  }

  @Test
  void of_collectPreservesElementsAndOrder() {
    List<String> result = DataPipeline.of(List.of("x", "y", "z")).collect();

    assertThat(result).containsExactly("x", "y", "z");
  }

  @Test
  void extract_beginsPipelineFromExtractor() {
    DataPipeline.Extractor<Integer> extractor = () -> List.of(1, 2, 3);

    assertThat(DataPipeline.extract(extractor).collect()).containsExactly(1, 2, 3);
  }

  @Test
  void transform_chainsAcrossTypeChanges() {
    List<Integer> lengths =
        DataPipeline.of(List.of(1, 22, 333))
            .transform(String::valueOf)
            .transform(String::length)
            .collect();

    assertThat(lengths).containsExactly(1, 2, 3);
  }

  @Test
  void load_deliversFullBatchesOfRequestedSize() {
    List<Integer> batchSizes = new ArrayList<>();

    int total =
        DataPipeline.of(List.of(1, 2, 3, 4, 5, 6)).load(batch -> batchSizes.add(batch.size()), 3);

    assertThat(total).isEqualTo(6);
    assertThat(batchSizes).containsExactly(3, 3);
  }

  @Test
  void load_batchSizeLargerThanData_deliversSingleBatch() {
    List<Collection<Integer>> batches = new ArrayList<>();

    int total =
        DataPipeline.of(List.of(1, 2, 3)).load(batch -> batches.add(new ArrayList<>(batch)), 100);

    assertThat(total).isEqualTo(3);
    assertThat(batches).hasSize(1);
    assertThat(batches.getFirst()).containsExactly(1, 2, 3);
  }

  @Test
  void emptyPipeline_collectsToEmptyListAndNeverInvokesLoader() {
    AtomicInteger loaderCalls = new AtomicInteger();
    DataPipeline<String> pipeline = DataPipeline.of(List.of());

    int total = pipeline.load(batch -> loaderCalls.incrementAndGet(), 10);

    assertThat(pipeline.collect()).isEmpty();
    assertThat(total).isZero();
    assertThat(loaderCalls).hasValue(0);
  }

  @Test
  void filter_rejectingEverythingYieldsEmptyResultAndZeroLoads() {
    AtomicInteger loaderCalls = new AtomicInteger();

    DataPipeline<Integer> filtered = DataPipeline.of(List.of(1, 2, 3)).filter(n -> n > 10);

    assertThat(filtered.collect()).isEmpty();
    assertThat(filtered.load(batch -> loaderCalls.incrementAndGet(), 2)).isZero();
    assertThat(loaderCalls).hasValue(0);
  }

  @Test
  void load_rejectsNonPositiveBatchSize() {
    DataPipeline<Integer> pipeline = DataPipeline.of(List.of(1, 2, 3));

    assertThatIllegalArgumentException()
        .isThrownBy(() -> pipeline.load(batch -> {}, 0))
        .withMessageContaining("batchSize");
    assertThatIllegalArgumentException().isThrownBy(() -> pipeline.load(batch -> {}, -1));
  }
}

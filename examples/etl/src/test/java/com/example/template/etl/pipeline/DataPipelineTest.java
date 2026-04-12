package com.example.template.etl.pipeline;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataPipelineTest {

    @Test
    void filterAndTransform() {
        List<Integer> result = DataPipeline.of(List.of(1, 2, 3, 4, 5))
                .filter(n -> n % 2 == 0)
                .transform(n -> n * 10)
                .collect();

        assertThat(result).containsExactly(20, 40);
    }

    @Test
    void loadInBatches() {
        List<Collection<String>> batches = new ArrayList<>();

        int total = DataPipeline.of(List.of("a", "b", "c", "d", "e"))
                .load(batch -> batches.add(new ArrayList<>(batch)), 2);

        assertThat(total).isEqualTo(5);
        assertThat(batches).hasSize(3); // [a,b], [c,d], [e]
        assertThat(batches.get(0)).containsExactly("a", "b");
        assertThat(batches.get(2)).containsExactly("e");
    }

    @Test
    void extractFilterTransformLoad() {
        record Sale(String product, double amount) {}

        List<Collection<String>> loaded = new ArrayList<>();

        int count = DataPipeline.<Sale>extract(() -> List.of(
                        new Sale("A", 100), new Sale("B", 50), new Sale("C", 200)))
                .filter(s -> s.amount() >= 100)
                .transform(s -> s.product() + ":" + s.amount())
                .load(batch -> loaded.add(new ArrayList<>(batch)), 10);

        assertThat(count).isEqualTo(2);
        assertThat(loaded.getFirst()).containsExactly("A:100.0", "C:200.0");
    }
}

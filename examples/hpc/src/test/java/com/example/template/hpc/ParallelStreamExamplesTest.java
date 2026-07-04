package com.example.template.hpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ParallelStreamExamples}. */
@DisplayName("ParallelStreamExamples")
class ParallelStreamExamplesTest {

  @Test
  @DisplayName("sumOfSquares computes the sum for a small range")
  void sumOfSquaresComputesSmallRange() {
    assertThat(ParallelStreamExamples.sumOfSquares(10)).isEqualTo(385L);
  }

  @Test
  @DisplayName("sumOfSquares matches the closed-form n(n+1)(2n+1)/6")
  void sumOfSquaresMatchesClosedForm() {
    int limit = 10_000;
    long expected = (long) limit * (limit + 1) * (2L * limit + 1) / 6;

    assertThat(ParallelStreamExamples.sumOfSquares(limit)).isEqualTo(expected);
  }

  @Test
  @DisplayName("sumOfSquares returns zero for an empty range")
  void sumOfSquaresReturnsZeroForEmptyRange() {
    assertThat(ParallelStreamExamples.sumOfSquares(0)).isZero();
  }

  @Test
  @DisplayName("groupByClassParallel groups items by simple class name")
  void groupByClassParallelGroupsBySimpleClassName() throws Exception {
    List<Object> items = List.of("a", 1, "b", 2L, 3, "c");

    Map<String, List<Object>> grouped = ParallelStreamExamples.groupByClassParallel(items, 2);

    assertThat(grouped).containsOnlyKeys("String", "Integer", "Long");
    assertThat(grouped.get("String")).containsExactly("a", "b", "c");
    assertThat(grouped.get("Integer")).containsExactly(1, 3);
    assertThat(grouped.get("Long")).containsExactly(2L);
  }

  @Test
  @DisplayName("groupByModulo partitions numbers by remainder")
  void groupByModuloPartitionsNumbersByRemainder() {
    List<Integer> numbers = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

    ConcurrentMap<Integer, List<Integer>> grouped =
        ParallelStreamExamples.groupByModulo(numbers, 3);

    assertThat(grouped).containsOnlyKeys(0, 1, 2);
    assertThat(grouped.get(0)).containsExactlyInAnyOrder(0, 3, 6, 9);
    assertThat(grouped.get(1)).containsExactlyInAnyOrder(1, 4, 7);
    assertThat(grouped.get(2)).containsExactlyInAnyOrder(2, 5, 8);
  }

  @Test
  @DisplayName("firstNPrimes returns the first primes in encounter order")
  void firstNPrimesReturnsPrimesInEncounterOrder() {
    assertThat(ParallelStreamExamples.firstNPrimes(10))
        .containsExactly(2, 3, 5, 7, 11, 13, 17, 19, 23, 29);
  }

  @Test
  @DisplayName("firstNPrimesUnordered returns the requested number of distinct primes")
  void firstNPrimesUnorderedReturnsDistinctPrimes() {
    List<Integer> primes = ParallelStreamExamples.firstNPrimesUnordered(10);

    assertThat(primes).hasSize(10).doesNotHaveDuplicates();
    assertThat(primes).allSatisfy(p -> assertThat(isPrime(p)).isTrue());
  }

  private static boolean isPrime(int n) {
    if (n < 2) {
      return false;
    }
    for (int i = 2; i * i <= n; i++) {
      if (n % i == 0) {
        return false;
      }
    }
    return true;
  }
}

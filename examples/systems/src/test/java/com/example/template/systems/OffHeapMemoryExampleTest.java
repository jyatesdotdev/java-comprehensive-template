package com.example.template.systems;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for {@link OffHeapMemoryExample} direct buffer and off-heap array operations. */
class OffHeapMemoryExampleTest {

    @Test
    void directBufferDemo_returnsNonNullBuffer() {
        var buf = OffHeapMemoryExample.directBufferDemo(64);
        assertThat(buf).isNotNull();
        assertThat(buf.isDirect()).isTrue();
    }

    @Test
    void offHeapDoubleArray_readWriteRoundTrip() {
        try (var array = new OffHeapMemoryExample.OffHeapDoubleArray(10)) {
            array.set(0, 1.5);
            array.set(9, 99.9);

            assertThat(array.get(0)).isEqualTo(1.5);
            assertThat(array.get(9)).isEqualTo(99.9);
            assertThat(array.length()).isEqualTo(10);
        }
    }
}

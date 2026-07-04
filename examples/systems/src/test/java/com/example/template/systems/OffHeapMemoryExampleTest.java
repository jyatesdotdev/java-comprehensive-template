package com.example.template.systems;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.BufferOverflowException;
import java.nio.ByteOrder;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link OffHeapMemoryExample} direct buffer and off-heap array operations. */
class OffHeapMemoryExampleTest {

  @Test
  void directBufferDemo_returnsNonNullBuffer() {
    var buf = OffHeapMemoryExample.directBufferDemo(64);
    assertThat(buf).isNotNull();
    assertThat(buf.isDirect()).isTrue();
  }

  @Test
  void directBufferDemo_usesNativeByteOrder() {
    var buf = OffHeapMemoryExample.directBufferDemo(64);
    assertThat(buf.order()).isEqualTo(ByteOrder.nativeOrder());
  }

  @Test
  void directBufferDemo_capacityTooSmallForDemoDataThrows() {
    // The demo writes 20 bytes (int + double + long); smaller buffers overflow.
    assertThatThrownBy(() -> OffHeapMemoryExample.directBufferDemo(16))
        .isInstanceOf(BufferOverflowException.class);
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

  @Test
  void offHeapDoubleArray_closeIsIdempotent() {
    try (var array = new OffHeapMemoryExample.OffHeapDoubleArray(4)) {
      array.set(1, 2.5);

      // Cleaner.Cleanable.clean() runs at most once, so repeated close() is safe;
      // try-with-resources closes a third time on block exit.
      assertThatCode(array::close).doesNotThrowAnyException();
      assertThatCode(array::close).doesNotThrowAnyException();
      assertThat(array.length()).isEqualTo(4);
    }
  }

  @Test
  void offHeapDoubleArray_outOfBoundsIndexThrows() {
    try (var array = new OffHeapMemoryExample.OffHeapDoubleArray(3)) {
      assertThatThrownBy(() -> array.get(3)).isInstanceOf(IndexOutOfBoundsException.class);
      assertThatThrownBy(() -> array.set(3, 1.0)).isInstanceOf(IndexOutOfBoundsException.class);
      assertThatThrownBy(() -> array.get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }
  }
}

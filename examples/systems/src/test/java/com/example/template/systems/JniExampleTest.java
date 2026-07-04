package com.example.template.systems;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JniExample} covering the graceful-degradation pattern.
 *
 * <p>The repo ships no {@code native_example} library, so raw native calls are expected to throw
 * {@link UnsatisfiedLinkError}. All assertions are written to also pass if someone has built the
 * native library locally.
 */
class JniExampleTest {

  private final JniExample jni = new JniExample();

  @Test
  void fallbackAdd_addsTwoIntegers() {
    assertThat(jni.fallbackAdd(2, 3)).isEqualTo(5);
    assertThat(jni.fallbackAdd(-4, 4)).isZero();
    assertThat(jni.fallbackAdd(0, 0)).isZero();
    assertThat(jni.fallbackAdd(-10, -5)).isEqualTo(-15);
  }

  @Test
  void safeAdd_neverThrows_andMatchesFallbackResult() {
    // Must hold whether or not the native library is present: safeAdd falls back
    // to the pure-Java implementation, which must agree with fallbackAdd.
    assertThat(jni.safeAdd(2, 3)).isEqualTo(jni.fallbackAdd(2, 3));
    assertThat(jni.safeAdd(-7, 7)).isEqualTo(jni.fallbackAdd(-7, 7));
    assertThat(jni.safeAdd(1_000_000, 2_000_000)).isEqualTo(jni.fallbackAdd(1_000_000, 2_000_000));
  }

  @Test
  void safeAdd_returnsCorrectSum() {
    assertThat(jni.safeAdd(20, 22)).isEqualTo(42);
  }

  @Test
  void nativeAdd_throwsUnsatisfiedLinkError_whenLibraryNotBuilt() {
    Throwable thrown = catchThrowable(() -> jni.nativeAdd(1, 2));

    if (thrown == null) {
      // Native library was built locally: verify it agrees with the Java fallback.
      assertThat(jni.nativeAdd(1, 2)).isEqualTo(jni.fallbackAdd(1, 2));
    } else {
      // Default repo state: no native library ships, so the raw native call fails.
      assertThat(thrown).isInstanceOf(UnsatisfiedLinkError.class);
    }
  }
}

package com.example.template.systems;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Off-heap (direct) memory management in Java.
 *
 * <p>Off-heap memory lives outside the GC-managed heap. Benefits:
 * <ul>
 *   <li>No GC pauses for large buffers</li>
 *   <li>Direct I/O without copying (zero-copy networking, memory-mapped files)</li>
 *   <li>Predictable memory layout for interop with native code</li>
 * </ul>
 *
 * <p>Risks: manual lifecycle management, potential leaks, no bounds-check safety net.
 */
@SuppressWarnings("PMD.SystemPrintln") // Example code
public final class OffHeapMemoryExample {

    private static final Cleaner CLEANER = Cleaner.create();

    /**
     * Allocates a direct ByteBuffer (off-heap) and performs typed read/write.
     * Direct buffers are deallocated when the ByteBuffer is GC'd (via internal Cleaner).
     *
     * @param capacityBytes size of the direct buffer in bytes
     * @return the allocated direct {@link ByteBuffer} after read-back (position at end of data)
     */
    public static ByteBuffer directBufferDemo(int capacityBytes) {
        ByteBuffer buf = ByteBuffer.allocateDirect(capacityBytes)
                .order(ByteOrder.nativeOrder());

        // Write structured data
        buf.putInt(42);
        buf.putDouble(3.14159);
        buf.putLong(System.nanoTime());

        // Read back
        buf.flip();
        int i = buf.getInt();
        double d = buf.getDouble();
        long ts = buf.getLong();

        System.out.printf("Read from direct buffer: int=%d, double=%.5f, long=%d%n", i, d, ts);
        return buf;
    }

    /**
     * A simple off-heap array of doubles with deterministic cleanup via {@link Cleaner}.
     * Demonstrates the recommended Java 9+ pattern for releasing native resources.
     */
    @SuppressWarnings("PMD.UnusedLocalVariable") // ref captured by Cleaner lambda
    public static class OffHeapDoubleArray implements AutoCloseable {

        private final ByteBuffer buffer;
        private final int length;
        private final Cleaner.Cleanable cleanable;

        /**
         * Creates an off-heap double array of the given length.
         *
         * @param length number of double elements to allocate
         */
        public OffHeapDoubleArray(int length) {
            this.length = length;
            this.buffer = ByteBuffer.allocateDirect(length * Double.BYTES)
                    .order(ByteOrder.nativeOrder());
            // Register a Cleaner as safety net if close() is not called
            ByteBuffer ref = this.buffer;
            this.cleanable = CLEANER.register(this, () -> {
                // DirectByteBuffer is freed when GC'd; Cleaner ensures prompt release
                // For sun.misc.Cleaner-backed buffers this is a no-op, but the pattern
                // is correct for any native resource.
            });
        }

        /**
         * Stores a double value at the given index.
         *
         * @param index zero-based element index
         * @param value the double value to store
         */
        public void set(int index, double value) {
            buffer.putDouble(index * Double.BYTES, value);
        }

        /**
         * Reads the double value at the given index.
         *
         * @param index zero-based element index
         * @return the double value stored at {@code index}
         */
        public double get(int index) {
            return buffer.getDouble(index * Double.BYTES);
        }

        /**
         * Returns the number of double elements in this array.
         *
         * @return the array length
         */
        public int length() {
            return length;
        }

        @Override
        public void close() {
            cleanable.clean();
        }
    }

    /**
     * Memory-mapped file pattern (conceptual — requires a real file).
     *
     * <pre>{@code
     * try (var channel = FileChannel.open(path, READ, WRITE)) {
     *     MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_WRITE, 0, channel.size());
     *     mapped.putInt(0, 42);       // writes directly to file
     *     int val = mapped.getInt(0); // reads directly from file
     *     mapped.force();             // flush to disk
     * }
     * }</pre>
     */
    private OffHeapMemoryExample() {}
}

package com.example.template.systems;

import java.nio.file.Path;

/**
 * Demonstrates Java Native Interface (JNI) patterns.
 *
 * <p>JNI allows Java code to call (and be called by) native applications and libraries
 * written in C, C++, or assembly. Common use cases: hardware access, legacy library
 * integration, performance-critical native code.
 *
 * <h3>Workflow</h3>
 * <ol>
 *   <li>Declare {@code native} methods in Java</li>
 *   <li>Generate C header: {@code javac -h output/native src/.../JniExample.java}</li>
 *   <li>Implement the C functions matching the generated signatures</li>
 *   <li>Compile to shared library: {@code gcc -shared -o libnative.so -I$JAVA_HOME/include ...}</li>
 *   <li>Load at runtime via {@link System#loadLibrary(String)}</li>
 * </ol>
 *
 * <h3>Generated C Header (example)</h3>
 * <pre>{@code
 * // com_example_template_systems_JniExample.h
 * JNIEXPORT jint JNICALL Java_com_example_template_systems_JniExample_nativeAdd
 *   (JNIEnv *, jobject, jint, jint);
 *
 * JNIEXPORT jstring JNICALL Java_com_example_template_systems_JniExample_nativeGreet
 *   (JNIEnv *, jobject, jstring);
 * }</pre>
 */
@SuppressWarnings("PMD.SystemPrintln") // Example code
public class JniExample {

    // Load native library from java.library.path
    static {
        try {
            System.loadLibrary("native_example");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native library not found — JNI methods will throw. "
                    + "Build the C library first. See README for instructions.");
        }
    }

    /**
     * Native method: adds two integers in C.
     * Maps to: {@code Java_com_example_template_systems_JniExample_nativeAdd}
     *
     * @param a first operand
     * @param b second operand
     * @return the sum {@code a + b}
     */
    public native int nativeAdd(int a, int b);

    /**
     * Native method: returns a greeting string from C.
     *
     * @param name the name to greet
     * @return a greeting string produced by native code
     */
    public native String nativeGreet(String name);

    /**
     * Demonstrates loading a library from an explicit path instead of java.library.path.
     *
     * @param libraryPath absolute or relative path to the shared library file
     */
    public static void loadFromPath(Path libraryPath) {
        System.load(libraryPath.toAbsolutePath().toString());
    }

    /**
     * Pure-Java fallback — use when native library is unavailable.
     * This pattern lets you ship a Java fallback alongside native acceleration.
     *
     * @param a first operand
     * @param b second operand
     * @return the sum {@code a + b}
     */
    public int fallbackAdd(int a, int b) {
        return a + b;
    }

    /**
     * Safe wrapper that tries native, falls back to Java.
     *
     * @param a first operand
     * @param b second operand
     * @return the sum {@code a + b}, computed natively if possible
     */
    public int safeAdd(int a, int b) {
        try {
            return nativeAdd(a, b);
        } catch (UnsatisfiedLinkError e) {
            return fallbackAdd(a, b);
        }
    }
}

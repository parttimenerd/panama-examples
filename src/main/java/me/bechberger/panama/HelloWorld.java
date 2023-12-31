package me.bechberger.panama;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

/**
 * The equivalent Java program is in misc/read_line.c
 */
public class HelloWorld {
    public static void main(String[] args) {
        var file = fopen(args[0], "r");
        var line = gets(file, 1024);
        System.out.println(line);
        fclose(file);
    }

    private static final MethodHandle fopen = Linker.nativeLinker().downcallHandle(
            PanamaUtil.lookup("fopen"),
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

    public static MemorySegment fopen(String filename, String mode) {
        try (var arena = Arena.ofConfined()) {
            return (MemorySegment) fopen.invokeExact(
                    arena.allocateUtf8String(filename),
                    arena.allocateUtf8String(mode));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private static final MethodHandle fclose = Linker.nativeLinker().downcallHandle(
            PanamaUtil.lookup("fclose"),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

    public static int fclose(MemorySegment file) {
        try {
            return (int) fclose.invokeExact(file);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static final MethodHandle fgets = Linker.nativeLinker().downcallHandle(
            PanamaUtil.lookup("fgets"),
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

    public static String gets(MemorySegment file, int size) {
        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocateArray(ValueLayout.JAVA_BYTE, size);
            var ret = (MemorySegment) fgets.invokeExact(buffer, size, file);
            if (ret == MemorySegment.NULL) {
                return null;
            }
            return buffer.getUtf8String(0);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}

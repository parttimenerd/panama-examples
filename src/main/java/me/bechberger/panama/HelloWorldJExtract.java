package me.bechberger.panama;

import me.bechberger.panama.raw.Lib;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class HelloWorldJExtract {
    public static void main(String[] args) {
        var file = fopen(args[0], "r");
        var line = gets(file, 1024);
        System.out.println(line);
        fclose(file);
    }

public static MemorySegment fopen(String filename, String mode) {
    try (var arena = Arena.ofConfined()) {
        return Lib.fopen(
                arena.allocateUtf8String(filename),
                arena.allocateUtf8String(mode));
    }
}

    public static int fclose(MemorySegment file) {
        return Lib.fclose(file);
    }

    public static String gets(MemorySegment file, int size) {
        try (var arena = Arena.ofConfined()) {
            var buffer = arena.allocateArray(ValueLayout.JAVA_BYTE, size);
            var ret = Lib.fgets(buffer, size, file);
            if (ret == MemorySegment.NULL) {
                return null;
            }
            return buffer.getUtf8String(0);
        }
    }
}

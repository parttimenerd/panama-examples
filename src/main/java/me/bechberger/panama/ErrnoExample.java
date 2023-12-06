package me.bechberger.panama;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.util.function.Function;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static me.bechberger.panama.PanamaUtil.POINTER;
import static me.bechberger.panama.PanamaUtil.lookup;

public class ErrnoExample {

    public static void main(String[] args) {
        try (var arena = Arena.ofConfined()) {
            // declare the errno as state to be captured, directly after the downcall without any interence of the
            // JVM runtime
            StructLayout capturedStateLayout = Linker.Option.captureStateLayout();
            VarHandle errnoHandle = capturedStateLayout.varHandle(MemoryLayout.PathElement.groupElement("errno"));
            Linker.Option ccs = Linker.Option.captureCallState("errno");

            MethodHandle fopen = Linker.nativeLinker().downcallHandle(
                    lookup("fopen"), FunctionDescriptor.of(POINTER, POINTER, POINTER), ccs);

            MemorySegment capturedState = arena.allocate(capturedStateLayout);
            try {
                // reading a non-existent file, this will set the errno
                MemorySegment result = (MemorySegment) fopen.invoke(capturedState, arena.allocateUtf8String("nonexistent_file"),
                        arena.allocateUtf8String("r"));
                int errno = (int) errnoHandle.get(capturedState);
                System.out.println(errno + ": " + errnoString(errno));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    static String errnoString(int errno){
        AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(JAVA_BYTE));
        MethodHandle strerror = Linker.nativeLinker()
                .downcallHandle(lookup("strerror"),
                        FunctionDescriptor.of(POINTER, ValueLayout.JAVA_INT));
        try {
            MemorySegment str = (MemorySegment) strerror.invokeExact(errno);
            return str.getUtf8String(0);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}

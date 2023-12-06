package me.bechberger.panama;

import java.lang.foreign.*;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

public class PanamaUtil {
    private PanamaUtil() {}

    public static final AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(JAVA_BYTE));

    public static MemorySegment lookup(String symbol) {
        return Linker.nativeLinker().defaultLookup().find(symbol)
                .or(() -> SymbolLookup.loaderLookup().find(symbol)).orElseThrow();
    }

}

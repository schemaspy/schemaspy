package org.schemaspy.util;

import java.util.Objects;
import java.util.function.Supplier;

public class LazyString implements Supplier<String> {
    public static LazyString lazyString(Supplier<String> stringSupplier) {
        return new LazyString(stringSupplier);
    }

    private final Supplier<String> stringSupplier;
    private String string;

    public LazyString(Supplier<String> stringSupplier) {
        this.stringSupplier = stringSupplier;
    }

    @Override
    public String get() {
        if (Objects.isNull(string)) {
            string = stringSupplier.get();
        }
        return string;
    }

    @Override
    public String toString() {
        return get();
    }
}

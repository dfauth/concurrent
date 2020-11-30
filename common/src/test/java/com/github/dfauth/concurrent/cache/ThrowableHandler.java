package com.github.dfauth.concurrent.cache;

import java.util.function.Function;

public interface ThrowableHandler<T> extends Function<Throwable, T> {

    static ThrowableHandler<Void> noOp() {
        return t -> null;
    }
}
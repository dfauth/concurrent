package com.github.dfauth.concurrent.cache;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface ExceptionalRunnable extends Callable<Void> {

    default Void call() throws Exception {
        run();
        return null;
    }

    void run() throws Exception;
}

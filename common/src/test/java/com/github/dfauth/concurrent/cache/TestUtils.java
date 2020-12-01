package com.github.dfauth.concurrent.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static com.github.dfauth.concurrent.cache.ThrowableHandler.noOp;

public class TestUtils {

    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public static TestExecutor executors(int n) {
        return new TestExecutor(n);
    }

    public static <T> ThrowableHandler<T> propagationHandler() {
        return t -> {
            throw new RuntimeException(t);
        };
    }
    private static Runnable noOpFinalRunnable = () -> {};

    public static Runnable withExceptionLogging(ExceptionalRunnable r) {
        return withExceptionLogging(r, noOpFinalRunnable);
    }

    public static Runnable withExceptionLogging(ExceptionalRunnable r, Runnable finalRunnable) {
        return () -> tryCatch(r, propagationHandler(), finalRunnable);
    }

    public static <T> T tryCatch(Callable<T> c) {
        return tryCatch(c, propagationHandler(), noOpFinalRunnable);
    }

    public static <T> T tryCatch(Callable<T> c, ThrowableHandler<T> handler) {
        return tryCatch(c, handler, noOpFinalRunnable);
    }

    public static <T> T tryCatch(Callable<T> c, ThrowableHandler<T> handler, Runnable finalRunnable) {
        try {
            return c.call();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return handler.apply(t);
        } finally {
            finalRunnable.run();
        }
    }

    public static void tryCatchIgnore(Callable<Void> c) {
        tryCatch(c, noOp(), noOpFinalRunnable);
    }

}

package com.github.dfauth.concurrent.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class TryCatch {

    private static final Logger logger = LoggerFactory.getLogger(TryCatch.class);

    public static TestExecutor executors(int n) {
        return new TestExecutor(n);
    }

    public static Runnable wrap(Runnable r) {
        return wrap(r, () -> {});
    }

    public static Runnable wrap(Runnable r, Runnable finalRunnable) {
        return () -> tryCatch(r, finalRunnable);
    }

    public static void tryCatch(Runnable r, Runnable finalRunnable) {
        try {
            r.run();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            finalRunnable.run();
        }
    }

    public static class TestExecutor {

        private final int n;

        public TestExecutor(int n) {
            this.n = n;
        }

        public CompletableFuture<Void> execute(Runnable r) {
            CompletableFuture<Void> f = new CompletableFuture<Void>();
            ExecutorService runner = Executors.newFixedThreadPool(n);
            CountDownLatch latch = new CountDownLatch(n);
            for(int i=0; i<n; i++) {
                runner.submit(wrap(() -> r.run(), () -> {
                    latch.countDown();
                    if(latch.getCount() == 0) {
                        f.complete(null);
                    }
                }));
            }
            return f;
        }
    }
}

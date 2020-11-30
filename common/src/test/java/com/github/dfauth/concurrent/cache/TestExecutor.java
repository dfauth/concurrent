package com.github.dfauth.concurrent.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.dfauth.concurrent.cache.TestUtils.withExceptionLogging;

public class TestExecutor {

    private final int n;

    public TestExecutor(int n) {
        this.n = n;
    }

    public CompletableFuture<Double> execute(Runnable r) {
        long now = System.nanoTime();
        CompletableFuture<Double> f = new CompletableFuture<>();
        ExecutorService runner = Executors.newFixedThreadPool(n);
        CountDownLatch latch = new CountDownLatch(n);
        for(int i=0; i<n; i++) {
            runner.submit(withExceptionLogging(() -> r.run(), () -> {
                latch.countDown();
                if(latch.getCount() == 0) {
                    f.complete((System.nanoTime()-now)/1000000.0);
                }
            }));
        }
        return f;
    }
}

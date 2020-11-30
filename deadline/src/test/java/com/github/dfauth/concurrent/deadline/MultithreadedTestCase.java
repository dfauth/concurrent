package com.github.dfauth.concurrent.deadline;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.github.dfauth.concurrent.cache.TestUtils.executors;
import static com.github.dfauth.concurrent.cache.TestUtils.withExceptionLogging;
import static java.lang.Math.random;
import static java.time.Instant.now;
import static org.junit.Assert.assertEquals;

public class MultithreadedTestCase {

    private static final Logger logger = LoggerFactory.getLogger(MultithreadedTestCase.class);

    @Before
    public void setUp() {
    }

    @Test
    public void testThreadsafe() throws InterruptedException, ExecutionException, TimeoutException {

        TestHandler handler = new TestHandler();
        DeadlineEngine engine = new DeadlineEngineImpl();
        assertEquals(0, engine.size());

        CompletableFuture<Double> f = executors(10).execute(withExceptionLogging(() -> {
            for (int i = 0; i < 10; i++) {
                Thread.sleep((long)(random() * 100));
                long t = now().plus(Duration.ofSeconds(5)).toEpochMilli();
                {
                    long id = engine.schedule(t);
                    logger.info("scheduled requestId {} for {}", id, t);
                }
                {
                    long id = engine.schedule(t);
                    logger.info("scheduled requestId {} for {}", id, t);
                }
                Thread.sleep(100);
            }
            while (engine.size() > 0) {
                Thread.sleep((long)(random() * 100));
                Thread.sleep(100);
                int cnt = engine.poll(now().toEpochMilli(), handler, Integer.MAX_VALUE);
                logger.info("executed {} callbacks", cnt);
            }
        }));
        f.thenAccept(e -> {
            logger.info("elapsed time: {} msec", e);
        });
        f.get(10, TimeUnit.SECONDS);
        for (int i = 0; i < 10; i++) {
            assertEquals("Oops, for requestId "+i, 1, handler.executionCount(i));
        }
    }

}

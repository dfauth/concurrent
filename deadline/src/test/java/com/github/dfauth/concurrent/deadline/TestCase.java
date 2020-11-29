package com.github.dfauth.concurrent.deadline;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class TestCase {

    private static final Logger logger = LoggerFactory.getLogger(TestCase.class);

    @Test
    public void testScheduleCancel() {
        TestHandler handler = new TestHandler();
        DeadlineEngine engine = new DeadlineEngineImpl();
        assertEquals(0, engine.size());
        Instant now = Instant.now();
        Instant t = now.plus(Duration.ofSeconds(4));
        long requestId = engine.schedule(t.toEpochMilli());
        assertEquals(1, engine.size());
        engine.cancel(requestId);
        assertEquals(0, engine.size());
        engine.poll(now.plus(Duration.ofSeconds(6)).toEpochMilli(), handler, 1);
        assertFalse(handler.wasTriggered());
    }

    @Test
    public void testScheduleOne() {
        TestHandler handler = new TestHandler();
        DeadlineEngine engine = new DeadlineEngineImpl();
        assertEquals(0, engine.size());
        Instant now = Instant.now();
        long t = now.plus(Duration.ofSeconds(4)).toEpochMilli();
        long id = engine.schedule(t);
        assertEquals(engine.size(), 1);
        engine.poll(now.toEpochMilli(), handler, 1);
        assertFalse(handler.wasTriggered());
        engine.poll(now.plus(Duration.ofSeconds(3)).toEpochMilli(), handler, 1);
        assertFalse(handler.wasTriggered());
        engine.poll(now.plus(Duration.ofSeconds(6)).toEpochMilli(), handler, 1);
        assertTrue(handler.wasTriggered());
        assertTrue(handler.wasTriggeredWith(id));
    }

    @Test
    public void testScheduleTwo() {
        TestHandler handler = new TestHandler();
        DeadlineEngine engine = new DeadlineEngineImpl();
        assertEquals(0, engine.size());
        Instant now = Instant.now();
        long id1 = engine.schedule(now.plus(Duration.ofSeconds(4)).toEpochMilli());
        assertEquals(1, engine.size());
        long id2 = engine.schedule(now.plus(Duration.ofSeconds(6)).toEpochMilli());
        assertEquals(2, engine.size());
        engine.poll(now.toEpochMilli(), handler, 1);
        assertFalse(handler.wasTriggered());
        engine.poll(now.plus(Duration.ofSeconds(3)).toEpochMilli(), handler, 1);
        assertFalse(handler.wasTriggered());
        engine.poll(now.plus(Duration.ofSeconds(5)).toEpochMilli(), handler, 1);
        assertTrue(handler.wasTriggered());
        assertTrue(handler.wasTriggeredWith(id1));
        assertFalse(handler.wasTriggeredWith(id2));
        engine.poll(now.plus(Duration.ofSeconds(7)).toEpochMilli(), handler, 1);
        assertTrue(handler.wasTriggered());
        assertTrue(handler.wasTriggeredWith(id1));
        assertTrue(handler.wasTriggeredWith(id2));
    }

    @Test
    public void testMaxPoll() {
        TestHandler handler = new TestHandler();
        DeadlineEngine engine = new DeadlineEngineImpl();
        assertEquals(0, engine.size());
        Instant now = Instant.now();
        long t = now.toEpochMilli();
        long id1 = engine.schedule(t);
        assertEquals(1, engine.size());
        long id2 = engine.schedule(t);
        assertEquals(2, engine.size());
        long t1 = now.plus(Duration.ofSeconds(1)).toEpochMilli();
        engine.poll(t1, handler, 1);
        assertEquals(1, engine.size());
        assertTrue(handler.wasTriggered());
        assertEquals(handler.triggeredRequestIds().size(), 1);
        assertTrue(handler.wasTriggeredWith(id1) || handler.wasTriggeredWith(id2));

        engine.poll(t1, handler, 1);
        assertEquals(0, engine.size());
        assertTrue(handler.wasTriggered());
        assertEquals(handler.triggeredRequestIds().size(), 2);
        assertTrue(handler.wasTriggeredWith(id1) && handler.wasTriggeredWith(id2));

    }

    static class TestHandler implements Consumer<Long> {

        private Collection<Long> requestIds = new ArrayList<>();

        @Override
        public void accept(Long id) {
            requestIds.add(id);
        }

        public boolean wasTriggered() {
            return ! requestIds.isEmpty();
        }

        public boolean wasTriggeredWith(Long requestId) {
            return requestIds.contains(requestId);
        }

        public boolean wasTriggeredAndReset() {
            try {
                return wasTriggered();
            } finally {
                requestIds.clear();
            }
        }

        public boolean wasTriggeredWithAndReset(Long requestId) {
            try {
                return wasTriggeredWith(requestId);
            } finally {
                requestIds.clear();
            }
        }

        public Collection<Long> triggeredRequestIds() {
            return requestIds;
        }
    }
}

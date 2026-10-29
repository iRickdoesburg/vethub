package dev.ilionx.workshop.common.ratelimit;

import io.github.jframe.exception.core.RateLimitExceededException;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Simple in-memory, fixed-window rate limiter keyed by an arbitrary caller-supplied key (e.g. a
 * client IP address).
 *
 * <p>Intended for low-volume, single-instance demo use (e.g. protecting a public, unauthenticated
 * lookup endpoint from brute-forcing). Not suitable as-is for a multi-instance deployment, since
 * state is held in local memory rather than a shared store.
 */
@Component
public class SimpleRateLimiter {

    private final Map<String, Deque<Instant>> hitsByKey = new ConcurrentHashMap<>();

    private final Map<String, Lock> locksByKey = new ConcurrentHashMap<>();

    private final int limit;

    private final long windowSeconds;

    /**
     * Constructs the rate limiter with the configured limit and window.
     *
     * @param limit         the maximum number of requests allowed per key within the window
     * @param windowSeconds the length of the fixed window, in seconds
     */
    public SimpleRateLimiter(
                             @Value("${security.rate-limit.visit-lookup.limit:10}") final int limit,
                             @Value("${security.rate-limit.visit-lookup.window-seconds:60}") final long windowSeconds
    ) {
        this.limit = limit;
        this.windowSeconds = windowSeconds;
    }

    /**
     * Records a hit for the given key and throws if the configured limit has been exceeded within
     * the current window.
     *
     * @param key the caller-supplied identifier (e.g. client IP address)
     */
    public void checkLimit(final String key) {
        final Instant now = Instant.now();
        final Duration window = Duration.ofSeconds(windowSeconds);
        final Deque<Instant> hits = hitsByKey.computeIfAbsent(key, ignored -> new ConcurrentLinkedDeque<>());
        final Lock lock = locksByKey.computeIfAbsent(key, ignored -> new ReentrantLock());

        lock.lock();
        try {
            while (!hits.isEmpty() && hits.peekFirst().isBefore(now.minus(window))) {
                hits.pollFirst();
            }

            if (hits.size() >= limit) {
                final OffsetDateTime resetDate = OffsetDateTime.ofInstant(hits.peekFirst().plus(window), ZoneOffset.UTC);
                throw new RateLimitExceededException(limit, 0, resetDate);
            }

            hits.addLast(now);
        } finally {
            lock.unlock();
        }
    }
}

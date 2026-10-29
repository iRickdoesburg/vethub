package dev.ilionx.workshop.common.ratelimit;

import dev.ilionx.workshop.support.UnitTest;
import io.github.jframe.exception.core.RateLimitExceededException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Unit Test - Simple Rate Limiter")
class SimpleRateLimiterTest extends UnitTest {

    @Test
    @DisplayName("Should allow requests under the configured limit")
    void shouldAllowRequestsUnderTheConfiguredLimit() {
        // Given: A rate limiter allowing 3 requests per 60-second window
        final SimpleRateLimiter rateLimiter = new SimpleRateLimiter(3, 60);

        // When: Checking the limit 3 times for the same key
        // Then: None of the calls should throw
        rateLimiter.checkLimit("127.0.0.1");
        rateLimiter.checkLimit("127.0.0.1");
        rateLimiter.checkLimit("127.0.0.1");
    }

    @Test
    @DisplayName("Should reject requests once the limit is exceeded")
    void shouldRejectRequestsOnceTheLimitIsExceeded() {
        // Given: A rate limiter allowing 2 requests per 60-second window
        final SimpleRateLimiter rateLimiter = new SimpleRateLimiter(2, 60);

        // When: Checking the limit 3 times for the same key
        rateLimiter.checkLimit("127.0.0.1");
        rateLimiter.checkLimit("127.0.0.1");

        // Then: The third call should be rejected with a rate-limit exception
        final RateLimitExceededException exception = assertThrows(
            RateLimitExceededException.class,
            () -> rateLimiter.checkLimit("127.0.0.1")
        );
        assertThat(exception.getLimit(), is(2));
    }

    @Test
    @DisplayName("Should track separate windows per key")
    void shouldTrackSeparateWindowsPerKey() {
        // Given: A rate limiter allowing only 1 request per 60-second window
        final SimpleRateLimiter rateLimiter = new SimpleRateLimiter(1, 60);

        // When: Two different keys each make one request
        rateLimiter.checkLimit("127.0.0.1");
        rateLimiter.checkLimit("10.0.0.5");

        // Then: Neither call should throw, since they are tracked independently
    }
}

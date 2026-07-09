package org.app.f1.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

public class RateLimiter extends OncePerRequestFilter {

    private static class Bucket {

        private final double refillRatePerSecond;
        private final double maxCapacity;

        private double tokens;
        private long lastRefillTimestamp;

        public Bucket(double rps, double maxBurstCapacity) {
            this.refillRatePerSecond = rps;
            this.maxCapacity = maxBurstCapacity;
            this.tokens = maxBurstCapacity;
            this.lastRefillTimestamp = System.nanoTime();
        }

        public synchronized boolean tryAcquire() {
            refill();

            if (tokens >= 1) {
                tokens -= 1;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastRefillTimestamp) / 1_000_000_000.0;

            if (elapsedSeconds > 0) {
                tokens = Math.min(maxCapacity, tokens + (elapsedSeconds * refillRatePerSecond));
                lastRefillTimestamp = now;
            }
        }

    }

    private static final int RPS = 5;
    private final Cache<String, Bucket> rateLimiters = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String ip = request.getRemoteAddr();

        Bucket bucket = rateLimiters.get(ip, key -> new Bucket(RPS, 15));

        if (!bucket.tryAcquire()) {
            response.setStatus(429);
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}

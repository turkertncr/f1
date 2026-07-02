package org.app.f1.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter extends OncePerRequestFilter {

    private static class RequestCount {
        final long second;
        int count;
        RequestCount(long second) {
            this.second = second;
            this.count = 1;
        }
    }

    private static final int RPS = 5;
    private final ConcurrentHashMap<String, RequestCount> rateLimiters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String ip = request.getRemoteAddr();
        long current = System.currentTimeMillis() / 1000;

        AtomicInteger snapshot = new AtomicInteger();
        rateLimiters.compute(ip, (key, req) -> {
            if (req == null || current != req.second) {
                snapshot.set(1);
                return new RequestCount(current);
            }
            snapshot.set(++req.count);
            return req;
        });

        if (snapshot.get() > RPS) {
            response.setStatus(429);
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }
        filterChain.doFilter(request, response);
    }
}

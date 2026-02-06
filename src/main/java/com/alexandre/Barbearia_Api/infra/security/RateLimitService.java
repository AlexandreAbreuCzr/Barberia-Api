package com.alexandre.Barbearia_Api.infra.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static class Counter {
        private int count;
        private long windowStart;
    }

    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public void check(String key, int limit, Duration window) {
        long now = System.currentTimeMillis();
        long windowMs = window.toMillis();

        Counter counter = counters.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart > windowMs) {
                Counter fresh = new Counter();
                fresh.count = 1;
                fresh.windowStart = now;
                return fresh;
            }
            if (existing.count < limit) {
                existing.count++;
            }
            return existing;
        });

        if (counter.count > limit) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Muitas tentativas. Aguarde e tente novamente."
            );
        }
    }
}

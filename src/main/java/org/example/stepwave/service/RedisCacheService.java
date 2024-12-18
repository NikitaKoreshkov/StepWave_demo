package org.example.stepwave.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedisCacheService {

    private final Map<String, String> cache = new HashMap<>();

    public void cachePassword(String username, String hashedPassword) {
        cache.put(username, hashedPassword);
    }

    public String getCachedPassword(String username) {
        return cache.get(username);
    }
}
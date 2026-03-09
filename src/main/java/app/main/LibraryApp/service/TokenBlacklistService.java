package app.main.LibraryApp.service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final ConcurrentHashMap<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();
    private final JwtService jwtService;

    public TokenBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token) {
        purgeExpired();
        blacklistedTokens.put(token, jwtService.extractExpiration(token));
    }

    public boolean isBlacklisted(String token) {
        purgeExpired();
        return blacklistedTokens.containsKey(token);
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
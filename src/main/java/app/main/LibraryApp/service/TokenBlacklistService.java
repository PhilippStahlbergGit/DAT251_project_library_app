package app.main.LibraryApp.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private final Set<String> blacklistedTokens = new HashSet<>();

    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
        System.out.println(">>> Token blacklisted. Blacklist size: " + blacklistedTokens.size());
        System.out.println(">>> Token: " + token);
    }

    public boolean isBlacklisted(String token) {
        boolean result = blacklistedTokens.contains(token);
        System.out.println(">>> Checking blacklist. Result: " + result);
        System.out.println(">>> Token: " + token);
        return result;
    }
}
package com.smartcampus;

public class AuthService {
    private int failedAttempts = 0;

    public boolean login(String email, String password) {
        if ("student@usms.ac.ma".equals(email) && "secret123".equals(password)) {
            failedAttempts = 0;
            return true;
        }
        failedAttempts++;
        return false;
    }

    public int getFailedAttempts() { return failedAttempts; }
    
}

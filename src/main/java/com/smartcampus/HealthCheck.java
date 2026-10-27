package com.smartcampus;

import java.net.HttpURLConnection;
import java.net.URI;

/** Used by Docker HEALTHCHECK: exit code 0 = healthy, 1 = unhealthy. */
public class HealthCheck {
    public static void main(String[] args) {
        String port = System.getenv().getOrDefault("PORT", "8080");
        try {
            HttpURLConnection c = (HttpURLConnection) URI.create("http://localhost:" + port + "/health").toURL().openConnection();
            c.setConnectTimeout(3000);
            c.setReadTimeout(4000);
            System.exit(c.getResponseCode() == 200 ? 0 : 1);
        } catch (Exception e) {
            System.exit(1);
        }
    }
}

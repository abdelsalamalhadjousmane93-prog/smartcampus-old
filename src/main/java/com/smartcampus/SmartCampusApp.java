package com.smartcampus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class SmartCampusApp {
    private static final AuthService AUTH = new AuthService();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(Config.get("PORT", "8082"));
        Database.init();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", ex -> {
            if ("/".equals(ex.getRequestURI().getPath())) {
                send(ex, 200, "SmartCampus API is running\n");
            } else {
                send(ex, 404, "Not found\n");
            }
        });
        server.createContext("/health", SmartCampusApp::health);
        server.createContext("/login", SmartCampusApp::login);
        server.start();
        System.out.println("SmartCampus started on port " + port);
    }

    private static void health(HttpExchange ex) throws IOException {
        boolean dbUp = Database.isUp();
        String db = !Database.configured() ? "NOT_CONFIGURED" : (dbUp ? "UP" : "DOWN");
        String body = "{\"status\":\"" + (dbUp ? "UP" : "DEGRADED") + "\",\"db\":\"" + db + "\"}\n";
        send(ex, dbUp ? 200 : 503, body);
    }

    private static synchronized void login(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            send(ex, 405, "Use POST with email and password\n");
            return;
        }
        String raw = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseForm(raw);
        String email = form.getOrDefault("email", "");
        String password = form.getOrDefault("password", "");

        if (AUTH.login(email, password)) {
            send(ex, 200, "Login OK\n");
            return;
        }
        if (AUTH.getFailedAttempts() == 3) {
            SecurityLog.record("3 consecutive failed logins for " + email);
        }
        send(ex, 401, "Invalid email or password\n");
    }

    private static Map<String, String> parseForm(String raw) {
        Map<String, String> map = new HashMap<>();
        for (String pair : raw.split("&")) {
            if (pair.isEmpty()) continue;
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, value);
        }
        return map;
    }

    private static void send(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}

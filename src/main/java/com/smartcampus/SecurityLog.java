package com.smartcampus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

/** Records security events (SCRUM-8: 3 consecutive failed logins). */
final class SecurityLog {
    private SecurityLog() {}

    static void record(String message) {
        String line = Instant.now() + " SECURITY " + message + System.lineSeparator();
        System.out.print(line);
        try {
            Path dir = Path.of(Config.get("DATA_DIR", "./data"));
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("security.log"), line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Could not write security.log: " + e.getMessage());
        }
        Database.insertEvent(message);
    }
}

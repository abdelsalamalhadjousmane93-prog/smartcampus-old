package com.smartcampus;

/** Reads settings from environment variables (12-factor style). */
final class Config {
    private Config() {}

    static String get(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}

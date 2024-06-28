package me.jtx.evobases.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class Cooldown {
    private JsonArray cooldowns;

    public Cooldown() {
        cooldowns = new JsonArray();
        loadCooldown();
    }

    public void addCooldown(String userId) {
        removeExpiredCooldowns();

        JsonObject existingCooldown = null;
        for (JsonElement element : cooldowns) {
            JsonObject cooldown = element.getAsJsonObject();
            if (cooldown.get("userId").getAsString().equals(userId)) {
                existingCooldown = cooldown;
                break;
            }
        }

        if (existingCooldown != null) {
            existingCooldown.addProperty("startTime", Instant.now().toEpochMilli());
            existingCooldown.addProperty("duration", 7 * 24 * 60 * 60 * 1000L); // 30 seconds in milliseconds
        } else {
            JsonObject newCooldown = new JsonObject();
            newCooldown.addProperty("userId", userId);
            newCooldown.addProperty("startTime", Instant.now().toEpochMilli());
            newCooldown.addProperty("duration", 7 * 24 * 60 * 60 * 1000L); // 30 seconds in milliseconds
            cooldowns.add(newCooldown);
        }

        saveCooldown();
    }

    public boolean isOnCooldown(String userId) {
        removeExpiredCooldowns();

        for (JsonElement element : cooldowns) {
            JsonObject cooldown = element.getAsJsonObject();
            if (cooldown.get("userId").getAsString().equals(userId)) {
                long startTime = cooldown.get("startTime").getAsLong();
                long duration = cooldown.get("duration").getAsLong();
                return Instant.now().toEpochMilli() < startTime + duration;
            }
        }
        return false;
    }

    public long getRemainingCooldownTime(String userId) {
        removeExpiredCooldowns();

        for (JsonElement element : cooldowns) {
            JsonObject cooldown = element.getAsJsonObject();
            if (cooldown.get("userId").getAsString().equals(userId)) {
                long startTime = cooldown.get("startTime").getAsLong();
                long duration = cooldown.get("duration").getAsLong();
                long now = Instant.now().toEpochMilli();
                long remainingTime = (startTime + duration) - now;
                return remainingTime > 0 ? remainingTime : 0;
            }
        }
        return 0;
    }

    public String formatCooldownTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long days = seconds / (24 * 3600);
        seconds %= 24 * 3600;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }

    private void removeExpiredCooldowns() {
        long now = Instant.now().toEpochMilli();
        JsonArray newCooldowns = new JsonArray();

        for (JsonElement element : cooldowns) {
            JsonObject cooldown = element.getAsJsonObject();
            long startTime = cooldown.get("startTime").getAsLong();
            long duration = cooldown.get("duration").getAsLong();
            if (now < startTime + duration) {
                newCooldowns.add(cooldown);
            }
        }

        cooldowns = newCooldowns;
    }

    public void loadCooldown() {
        try {
            Path path = Paths.get("Cooldown.json");
            if (!Files.exists(path)) {
                Files.createFile(path);
                Files.write(path, "[]".getBytes());
            }

            File file = new File("Cooldown.json");
            JsonArray newCooldown = new Gson().fromJson(new FileReader(file), JsonArray.class);
            if (newCooldown != null) {
                cooldowns = newCooldown;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCooldown() {
        try {
            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(cooldowns);
            Path path = Paths.get("Cooldown.json");
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

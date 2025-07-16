package net.sanfonic.hivemind.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final  String CONFIG_FILE_NAME = "hivemind.json";
    private static ModConfig INSTANCE;

    // Config Options
    public boolean enableDebugLogging = false;
    public boolean enableDroneLinkingDebug = false;
    public boolean enableCommandDebug = false;
    public int logCooldownSeconds = 5;
    public double droneFollowRange = 16.0;
    public double droneHealth = 40.0;
    public double droneSpeed = 0.25;

    // Transient fields (not saved to config)
    private transient Path configPath;

    private ModConfig() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
    }

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ModConfig();
            INSTANCE.load();
        }
        return INSTANCE;
    }

    public void load() {
        try {
            if (Files.exists(configPath)) {
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                    if (loaded != null) {
                        // Copy loaded values to this instance
                        this.enableDebugLogging = loaded.enableDebugLogging;
                        this.enableDroneLinkingDebug = loaded.enableDroneLinkingDebug;
                        this.enableCommandDebug = loaded.enableCommandDebug;
                        this.logCooldownSeconds = loaded.logCooldownSeconds;
                        this.droneFollowRange = loaded.droneFollowRange;
                        this.droneHealth = loaded.droneHealth;
                        this.droneSpeed = loaded.droneSpeed;
                    }
                }
                System.out.println("[HiveMind] Config loaded from " + configPath);
            } else {
                // Create default config file
                save();
                System.out.println("[HiveMind] Created default config at " + configPath);
            }
        } catch (Exception e) {
            System.err.println("[HiveMind] Failed to load config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            // Create config directory if it doesn't exist
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(this, writer);
            }
            System.out.println("[HiveMind] Config saved to " + configPath);
        } catch (Exception e) {
            System.err.println("[HiveMind] Failed to save config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Convience methods for debug logging
    public void debugLog(String message) {
        if (enableDebugLogging) {
            System.out.println("[HiveMind DEBUG] " + message);
        }
    }

    public void droneLinkDebugLog(String message) {
        if (enableDroneLinkingDebug) {
            System.out.println("[HiveMind DRONE] " + message);
        }
    }

    public void commandDebugLog(String message) {
        if (enableCommandDebug) {
            System.out.println("[HiveMind CMD] " + message);
        }
    }

    // Getters for convenience
    public long getLogCooldownMillis() {
        return logCooldownSeconds * 1000L;
    }

    public boolean isDebugEnabled() {
        return enableDebugLogging;
    }

    public boolean isDroneLinkingDebugEnabled() {
        return enableDroneLinkingDebug;
    }

    public boolean isCommandDebugEnabled() {
        return enableCommandDebug;
    }
}
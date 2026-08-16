package com.arenzya.discordhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path path = FabricLoader.getInstance().getConfigDir().resolve("discord-hud.json");
    private final Logger logger;
    private DiscordHudConfig config = new DiscordHudConfig();

    public ConfigManager(Logger logger) {
        this.logger = logger;
    }

    public DiscordHudConfig get() {
        return config;
    }

    public void load() {
        if (!Files.exists(path)) {
            save();
            return;
        }

        try {
            DiscordHudConfig loaded = gson.fromJson(Files.readString(path), DiscordHudConfig.class);
            config = loaded == null ? new DiscordHudConfig() : loaded;
            sanitize();
        } catch (Exception e) {
            logger.warn("Could not read Discord HUD config; using defaults", e);
            config = new DiscordHudConfig();
        }
    }

    public void save() {
        sanitize();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(config));
        } catch (IOException e) {
            logger.warn("Could not save Discord HUD config", e);
        }
    }

    private void sanitize() {
        config.hudOpacity = clamp(config.hudOpacity, 0.35f, 1.0f);
        config.uiScale = clamp(config.uiScale, 0.8f, 1.25f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}

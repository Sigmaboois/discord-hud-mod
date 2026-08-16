package com.arenzya.discordhud;

import com.arenzya.discordhud.config.ConfigManager;
import com.arenzya.discordhud.discord.DiscordProvider;
import com.arenzya.discordhud.discord.UnavailableDiscordProvider;
import com.arenzya.discordhud.discord.WindowsDiscordProvider;
import com.arenzya.discordhud.keybind.Keybinds;
import com.arenzya.discordhud.ui.DiscordHudRenderer;
import com.arenzya.discordhud.ui.DiscordHudSettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiscordHudClient implements ClientModInitializer {
    public static final String MOD_ID = "discordhud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static DiscordHudClient instance;

    private ConfigManager config;
    private DiscordProvider provider;
    private DiscordHudRenderer renderer;

    public static DiscordHudClient instance() {
        if (instance == null) throw new IllegalStateException("Discord HUD has not initialized yet");
        return instance;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        config = new ConfigManager(LOGGER);
        config.load();
        provider = createProvider();

        renderer = new DiscordHudRenderer(this);
        new Keybinds(this).register();
        renderer.register();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                provider.close();
            } catch (Exception e) {
                LOGGER.debug("Provider shutdown failed", e);
            }
        }, "discord-hud-shutdown"));

        LOGGER.info("Discord HUD initialized in self-contained desktop mode");
    }

    private DiscordProvider createProvider() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            LOGGER.info("Discord desktop controls are currently available on Windows only");
            return new UnavailableDiscordProvider();
        }

        try {
            return new WindowsDiscordProvider(LOGGER);
        } catch (Exception | LinkageError e) {
            LOGGER.warn("Windows Discord integration could not be initialized; controls are disabled", e);
            return new UnavailableDiscordProvider();
        }
    }

    public ConfigManager config() {
        return config;
    }

    public DiscordProvider provider() {
        return provider;
    }

    public DiscordHudRenderer renderer() {
        return renderer;
    }

    public void openSettings() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new DiscordHudSettingsScreen(this, minecraft.screen));
    }

    public void previewIncomingCall() {
        renderer.previewIncomingCall("Teammate");
    }

    public boolean acceptCall() {
        if (renderer.dismissPreview()) return true;
        return provider.acceptCall();
    }

    public boolean declineCall() {
        if (renderer.dismissPreview()) return true;
        return provider.declineCall();
    }
}

package com.arenzya.discordhud.ui;

import com.arenzya.discordhud.DiscordHudClient;
import com.arenzya.discordhud.config.DiscordHudConfig;
import com.arenzya.discordhud.state.CallState;
import com.arenzya.discordhud.state.ConnectionState;
import com.arenzya.discordhud.state.DiscordSnapshot;
import com.arenzya.discordhud.util.Easing;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public final class DiscordHudRenderer {
    private final DiscordHudClient mod;
    private long lastFrameNanos = System.nanoTime();
    private float popupProgress;
    private long previewUntil;
    private String previewCaller = "Teammate";

    public DiscordHudRenderer(DiscordHudClient mod) {
        this.mod = mod;
    }

    public void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(DiscordHudClient.MOD_ID, "discord_status"),
                this::render
        );
    }

    public void previewIncomingCall(String caller) {
        previewCaller = caller == null || caller.isBlank() ? "Teammate" : caller;
        previewUntil = System.currentTimeMillis() + 8_000L;
    }

    public boolean dismissPreview() {
        if (previewUntil <= System.currentTimeMillis()) return false;
        previewUntil = 0L;
        return true;
    }

    private void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui) return;

        long now = System.nanoTime();
        float delta = Math.min(0.05f, (now - lastFrameNanos) / 1_000_000_000f);
        lastFrameNanos = now;

        DiscordHudConfig config = mod.config().get();
        DiscordSnapshot state = mod.provider().snapshot();

        if (config.hudEnabled) renderCompactHud(graphics, state, config);

        boolean preview = previewUntil > System.currentTimeMillis();
        boolean ringing = state.call() == CallState.RINGING || preview;
        float target = config.incomingCallPopup && ringing ? 1.0f : 0.0f;
        popupProgress = config.reducedMotion ? target : Easing.approach(popupProgress, target, 13.0f, delta);
        if (popupProgress > 0.01f) {
            String caller = preview ? previewCaller : state.callerName();
            renderCallPopup(graphics, caller, config, popupProgress, preview);
        }
    }

    private void renderCompactHud(GuiGraphics graphics, DiscordSnapshot state, DiscordHudConfig config) {
        int x = Math.max(4, config.hudX);
        int y = Math.max(4, config.hudY);
        int width = config.showDesktopStatus ? 150 : 110;
        int height = 32;
        float alpha = config.hudOpacity;

        UiDraw.shadow(graphics, x, y, width, height, alpha * 0.45f);
        UiDraw.panel(graphics, x, y, width, height, UiDraw.withAlpha(UiColors.PANEL, alpha));
        UiDraw.outline(graphics, x, y, width, height, UiDraw.withAlpha(UiColors.OUTLINE, alpha));

        int dot = state.connection() == ConnectionState.CONNECTED ? UiColors.SUCCESS : UiColors.DANGER;
        graphics.fill(x + 9, y + 13, x + 14, y + 18, UiDraw.withAlpha(dot, alpha));

        String mic = state.mutedKnown() ? (state.muted() ? "MIC OFF" : "MIC ON") : "MIC --";
        String ears = state.deafenedKnown() ? (state.deafened() ? "DEAFENED" : "LISTENING") : "AUDIO --";
        graphics.drawString(Minecraft.getInstance().font, mic, x + 21, y + 7, UiDraw.withAlpha(UiColors.TEXT, alpha), false);
        graphics.drawString(Minecraft.getInstance().font, ears, x + 21, y + 18, UiDraw.withAlpha(UiColors.MUTED_TEXT, alpha), false);

        if (config.showDesktopStatus) {
            String desktop = state.connection() == ConnectionState.CONNECTED ? "DISCORD" : "OFFLINE";
            int color = state.connection() == ConnectionState.CONNECTED ? UiColors.ACCENT : UiColors.MUTED_TEXT;
            graphics.drawString(Minecraft.getInstance().font, desktop, x + 103, y + 13, UiDraw.withAlpha(color, alpha), false);
        }
    }

    private void renderCallPopup(GuiGraphics graphics, String callerName, DiscordHudConfig config, float progress, boolean preview) {
        int screenWidth = graphics.guiWidth();
        int width = 230;
        int height = 62;
        int x = config.popupX < 0 ? (screenWidth - width) / 2 : config.popupX;
        int baseY = Math.max(6, config.popupY);
        int y = baseY - Math.round((1.0f - Easing.outCubic(progress)) * 14.0f);
        float alpha = Math.min(config.hudOpacity, progress);

        UiDraw.shadow(graphics, x, y, width, height, alpha * 0.55f);
        UiDraw.panel(graphics, x, y, width, height, UiDraw.withAlpha(UiColors.PANEL, alpha));
        UiDraw.outline(graphics, x, y, width, height, UiDraw.withAlpha(UiColors.OUTLINE, alpha));

        UiDraw.panel(graphics, x + 10, y + 13, 34, 34, UiDraw.withAlpha(UiColors.ACCENT, alpha));
        String caller = config.privacyMode ? "Discord caller" : trim(callerName, 20);
        graphics.drawString(Minecraft.getInstance().font, caller, x + 53, y + 14, UiDraw.withAlpha(UiColors.TEXT, alpha), false);
        graphics.drawString(Minecraft.getInstance().font, preview ? "Call card preview" : "Incoming Discord call", x + 53, y + 28, UiDraw.withAlpha(UiColors.MUTED_TEXT, alpha), false);
        graphics.drawString(Minecraft.getInstance().font, "Use your answer / decline binds", x + 53, y + 41, UiDraw.withAlpha(UiColors.MUTED_TEXT, alpha), false);
    }

    private static String trim(String value, int max) {
        if (value == null || value.isBlank()) return "Unknown";
        return value.length() <= max ? value : value.substring(0, Math.max(1, max - 1)) + "…";
    }
}

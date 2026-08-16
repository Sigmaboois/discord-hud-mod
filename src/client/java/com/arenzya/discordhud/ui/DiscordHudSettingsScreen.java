package com.arenzya.discordhud.ui;

import com.arenzya.discordhud.DiscordHudClient;
import com.arenzya.discordhud.config.DiscordHudConfig;
import com.arenzya.discordhud.state.ConnectionState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class DiscordHudSettingsScreen extends Screen {
    private final DiscordHudClient mod;
    private final Screen parent;
    private long openedAt = System.nanoTime();

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;

    public DiscordHudSettingsScreen(DiscordHudClient mod, Screen parent) {
        super(Component.literal("Discord HUD"));
        this.mod = mod;
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(430, width - 28);
        panelHeight = Math.min(278, height - 28);
        panelX = (width - panelWidth) / 2;
        panelY = (height - panelHeight) / 2;
        openedAt = System.nanoTime();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0xB0000000);

        float appear = mod.config().get().reducedMotion ? 1.0f : Math.min(1.0f, (System.nanoTime() - openedAt) / 180_000_000f);
        int offset = Math.round((1.0f - appear) * 8.0f);
        int y = panelY + offset;

        UiDraw.shadow(graphics, panelX, y, panelWidth, panelHeight, 0.5f * appear);
        UiDraw.panel(graphics, panelX, y, panelWidth, panelHeight, UiDraw.withAlpha(UiColors.PANEL_SOFT, appear));
        UiDraw.outline(graphics, panelX, y, panelWidth, panelHeight, UiDraw.withAlpha(UiColors.OUTLINE, appear));

        graphics.drawString(font, "Discord HUD", panelX + 18, y + 16, UiDraw.withAlpha(UiColors.TEXT, appear), false);
        graphics.drawString(font, "Fast controls without leaving the fight.", panelX + 18, y + 31, UiDraw.withAlpha(UiColors.MUTED_TEXT, appear), false);

        boolean connected = mod.provider().snapshot().connection() == ConnectionState.CONNECTED;
        drawStatusPill(graphics, panelX + panelWidth - 94, y + 14, connected ? "DISCORD" : "NOT FOUND", connected, appear);

        int left = panelX + 18;
        int right = panelX + panelWidth / 2 + 4;
        int rowY = y + 58;
        int cardWidth = panelWidth / 2 - 28;

        drawToggle(graphics, left, rowY, cardWidth, "HUD", "Compact voice status", mod.config().get().hudEnabled, mouseX, mouseY, appear);
        drawToggle(graphics, right, rowY, cardWidth, "Call card", "Incoming-call overlay", mod.config().get().incomingCallPopup, mouseX, mouseY, appear);
        drawToggle(graphics, left, rowY + 48, cardWidth, "Privacy", "Hide caller names", mod.config().get().privacyMode, mouseX, mouseY, appear);
        drawToggle(graphics, right, rowY + 48, cardWidth, "Reduced motion", "Instant transitions", mod.config().get().reducedMotion, mouseX, mouseY, appear);
        drawToggle(graphics, left, rowY + 96, cardWidth, "App status", "Show Discord detection", mod.config().get().showDesktopStatus, mouseX, mouseY, appear);

        int opacityPercent = Math.round(mod.config().get().hudOpacity * 100.0f);
        drawStepper(graphics, right, rowY + 96, cardWidth, "HUD opacity", opacityPercent + "%", mouseX, mouseY, appear);

        int actionY = y + panelHeight - 61;
        drawAction(graphics, left, actionY, 116, "Preview call", mouseX, mouseY, appear, true);
        drawAction(graphics, left + 124, actionY, 104, "Refresh", mouseX, mouseY, appear, true);
        drawAction(graphics, panelX + panelWidth - 96, actionY, 78, "Done", mouseX, mouseY, appear, true);

        graphics.drawString(font, "Screen share keybind in Discord: Ctrl+Shift+Alt+S", left, y + panelHeight - 18, UiDraw.withAlpha(UiColors.MUTED_TEXT, appear), false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return super.mouseClicked(event, doubleClick);

        double mx = event.x();
        double my = event.y();
        int y = panelY;
        int left = panelX + 18;
        int right = panelX + panelWidth / 2 + 4;
        int rowY = y + 58;
        int cardWidth = panelWidth / 2 - 28;
        DiscordHudConfig config = mod.config().get();

        if (hit(mx, my, left, rowY, cardWidth, 40)) config.hudEnabled = !config.hudEnabled;
        else if (hit(mx, my, right, rowY, cardWidth, 40)) config.incomingCallPopup = !config.incomingCallPopup;
        else if (hit(mx, my, left, rowY + 48, cardWidth, 40)) config.privacyMode = !config.privacyMode;
        else if (hit(mx, my, right, rowY + 48, cardWidth, 40)) config.reducedMotion = !config.reducedMotion;
        else if (hit(mx, my, left, rowY + 96, cardWidth, 40)) config.showDesktopStatus = !config.showDesktopStatus;
        else if (hit(mx, my, right + cardWidth - 55, rowY + 96, 24, 40)) config.hudOpacity = Math.max(0.35f, config.hudOpacity - 0.05f);
        else if (hit(mx, my, right + cardWidth - 27, rowY + 96, 24, 40)) config.hudOpacity = Math.min(1.0f, config.hudOpacity + 0.05f);
        else {
            int actionY = y + panelHeight - 61;
            if (hit(mx, my, left, actionY, 116, 28)) mod.previewIncomingCall();
            else if (hit(mx, my, left + 124, actionY, 104, 28)) mod.provider().refresh();
            else if (hit(mx, my, panelX + panelWidth - 96, actionY, 78, 28)) {
                onClose();
                return true;
            } else return super.mouseClicked(event, doubleClick);
        }

        mod.config().save();
        return true;
    }

    @Override
    public void onClose() {
        mod.config().save();
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawToggle(GuiGraphics graphics, int x, int y, int width, String title, String subtitle, boolean enabled, int mouseX, int mouseY, float alpha) {
        boolean hover = hit(mouseX, mouseY, x, y, width, 40);
        int panel = hover ? UiColors.CARD_HOVER : UiColors.CARD;
        UiDraw.panel(graphics, x, y, width, 40, UiDraw.withAlpha(panel, alpha));
        UiDraw.outline(graphics, x, y, width, 40, UiDraw.withAlpha(UiColors.OUTLINE, alpha));
        graphics.drawString(font, title, x + 10, y + 9, UiDraw.withAlpha(UiColors.TEXT, alpha), false);
        graphics.drawString(font, subtitle, x + 10, y + 23, UiDraw.withAlpha(UiColors.MUTED_TEXT, alpha), false);

        int toggleX = x + width - 35;
        int toggleColor = enabled ? UiColors.ACCENT : UiColors.TOGGLE_OFF;
        UiDraw.panel(graphics, toggleX, y + 13, 25, 14, UiDraw.withAlpha(toggleColor, alpha));
        int knobX = enabled ? toggleX + 14 : toggleX + 3;
        graphics.fill(knobX, y + 16, knobX + 7, y + 23, UiDraw.withAlpha(UiColors.TEXT, alpha));
    }

    private void drawStepper(GuiGraphics graphics, int x, int y, int width, String title, String value, int mouseX, int mouseY, float alpha) {
        boolean hover = hit(mouseX, mouseY, x, y, width, 40);
        UiDraw.panel(graphics, x, y, width, 40, UiDraw.withAlpha(hover ? UiColors.CARD_HOVER : UiColors.CARD, alpha));
        UiDraw.outline(graphics, x, y, width, 40, UiDraw.withAlpha(UiColors.OUTLINE, alpha));
        graphics.drawString(font, title, x + 10, y + 9, UiDraw.withAlpha(UiColors.TEXT, alpha), false);
        graphics.drawString(font, value, x + 10, y + 23, UiDraw.withAlpha(UiColors.MUTED_TEXT, alpha), false);
        drawSmallButton(graphics, x + width - 55, y + 8, 24, 24, "−", mouseX, mouseY, alpha);
        drawSmallButton(graphics, x + width - 27, y + 8, 24, 24, "+", mouseX, mouseY, alpha);
    }

    private void drawAction(GuiGraphics graphics, int x, int y, int width, String label, int mouseX, int mouseY, float alpha, boolean enabled) {
        boolean hover = enabled && hit(mouseX, mouseY, x, y, width, 28);
        int color = enabled ? (hover ? UiColors.ACCENT_HOVER : UiColors.CARD_HOVER) : UiColors.CARD;
        UiDraw.panel(graphics, x, y, width, 28, UiDraw.withAlpha(color, alpha));
        UiDraw.outline(graphics, x, y, width, 28, UiDraw.withAlpha(UiColors.OUTLINE, alpha));
        int tx = x + (width - font.width(label)) / 2;
        graphics.drawString(font, label, tx, y + 10, UiDraw.withAlpha(enabled ? UiColors.TEXT : UiColors.MUTED_TEXT, alpha), false);
    }

    private void drawSmallButton(GuiGraphics graphics, int x, int y, int width, int height, String label, int mouseX, int mouseY, float alpha) {
        boolean hover = hit(mouseX, mouseY, x, y, width, height);
        UiDraw.panel(graphics, x, y, width, height, UiDraw.withAlpha(hover ? UiColors.ACCENT : UiColors.TOGGLE_OFF, alpha));
        int tx = x + (width - font.width(label)) / 2;
        graphics.drawString(font, label, tx, y + 8, UiDraw.withAlpha(UiColors.TEXT, alpha), false);
    }

    private void drawStatusPill(GuiGraphics graphics, int x, int y, String text, boolean connected, float alpha) {
        int width = 76;
        UiDraw.panel(graphics, x, y, width, 20, UiDraw.withAlpha(UiColors.CARD, alpha));
        int dotColor = connected ? UiColors.SUCCESS : UiColors.DANGER;
        graphics.fill(x + 8, y + 8, x + 12, y + 12, UiDraw.withAlpha(dotColor, alpha));
        graphics.drawString(font, text, x + 18, y + 7, UiDraw.withAlpha(UiColors.MUTED_TEXT, alpha), false);
    }

    private static boolean hit(double mx, double my, int x, int y, int width, int height) {
        return mx >= x && mx < x + width && my >= y && my < y + height;
    }
}

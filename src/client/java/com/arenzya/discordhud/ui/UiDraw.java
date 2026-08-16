package com.arenzya.discordhud.ui;

import net.minecraft.client.gui.GuiGraphics;

public final class UiDraw {
    private UiDraw() {
    }

    public static void panel(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x + 4, y, x + width - 4, y + height, color);
        graphics.fill(x, y + 4, x + width, y + height - 4, color);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, color);
    }

    public static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x + 4, y, x + width - 4, y + 1, color);
        graphics.fill(x + 4, y + height - 1, x + width - 4, y + height, color);
        graphics.fill(x, y + 4, x + 1, y + height - 4, color);
        graphics.fill(x + width - 1, y + 4, x + width, y + height - 4, color);
    }

    public static void shadow(GuiGraphics graphics, int x, int y, int width, int height, float alpha) {
        int shadow = withAlpha(0xFF000000, alpha);
        panel(graphics, x + 2, y + 3, width, height, shadow);
    }

    public static int withAlpha(int argb, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(((argb >>> 24) & 0xFF) * alpha)));
        return (argb & 0x00FFFFFF) | (a << 24);
    }
}

package com.arenzya.discordhud.util;

public final class Easing {
    private Easing() {
    }

    public static float outCubic(float t) {
        float x = 1.0f - clamp01(t);
        return 1.0f - x * x * x;
    }

    public static float approach(float current, float target, float speed, float deltaSeconds) {
        float factor = 1.0f - (float) Math.exp(-speed * Math.max(0.0f, deltaSeconds));
        return current + (target - current) * factor;
    }

    public static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}

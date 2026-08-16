package com.arenzya.discordhud.config;

import com.google.gson.annotations.SerializedName;

public final class DiscordHudConfig {
    public boolean hudEnabled = true;
    public boolean incomingCallPopup = true;
    public boolean reducedMotion = false;
    public boolean privacyMode = false;
    public boolean showChannel = false;
    public boolean showCallTimer = true;

    @SerializedName(value = "showDesktopStatus", alternate = "showBridgeStatus")
    public boolean showDesktopStatus = true;

    public float hudOpacity = 0.92f;
    public float uiScale = 1.0f;
    public int hudX = 14;
    public int hudY = 14;
    public int popupX = -1;
    public int popupY = 18;
}

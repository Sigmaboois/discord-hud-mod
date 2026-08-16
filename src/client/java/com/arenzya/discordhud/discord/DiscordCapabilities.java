package com.arenzya.discordhud.discord;

public record DiscordCapabilities(
        boolean acceptCall,
        boolean declineCall,
        boolean mute,
        boolean deafen,
        boolean screenShare,
        boolean incomingCallDetection
) {
    public static DiscordCapabilities windowsDesktop() {
        return new DiscordCapabilities(true, true, true, true, true, false);
    }

    public static DiscordCapabilities unavailable() {
        return new DiscordCapabilities(false, false, false, false, false, false);
    }
}

package com.arenzya.discordhud.state;

public record DiscordSnapshot(
        ConnectionState connection,
        CallState call,
        String callerName,
        String channelName,
        boolean muted,
        boolean mutedKnown,
        boolean deafened,
        boolean deafenedKnown,
        boolean screenSharing,
        boolean screenSharingKnown,
        long callStartedAtMillis
) {
    public static DiscordSnapshot disconnected() {
        return new DiscordSnapshot(
                ConnectionState.DISCONNECTED,
                CallState.IDLE,
                "",
                "",
                false,
                false,
                false,
                false,
                false,
                false,
                0L
        );
    }
}

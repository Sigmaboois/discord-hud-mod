package com.arenzya.discordhud.discord;

import com.arenzya.discordhud.state.DiscordSnapshot;

public interface DiscordProvider extends AutoCloseable {
    DiscordSnapshot snapshot();

    DiscordCapabilities capabilities();

    void tick();

    boolean acceptCall();

    boolean declineCall();

    boolean toggleMute();

    boolean toggleDeafen();

    boolean toggleScreenShare();

    void refresh();

    @Override
    void close();
}

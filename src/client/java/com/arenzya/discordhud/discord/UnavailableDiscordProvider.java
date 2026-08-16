package com.arenzya.discordhud.discord;

import com.arenzya.discordhud.state.DiscordSnapshot;

public final class UnavailableDiscordProvider implements DiscordProvider {
    @Override
    public DiscordSnapshot snapshot() {
        return DiscordSnapshot.disconnected();
    }

    @Override
    public DiscordCapabilities capabilities() {
        return DiscordCapabilities.unavailable();
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean acceptCall() {
        return false;
    }

    @Override
    public boolean declineCall() {
        return false;
    }

    @Override
    public boolean toggleMute() {
        return false;
    }

    @Override
    public boolean toggleDeafen() {
        return false;
    }

    @Override
    public boolean toggleScreenShare() {
        return false;
    }

    @Override
    public void refresh() {
    }

    @Override
    public void close() {
    }
}

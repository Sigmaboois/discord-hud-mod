package com.arenzya.discordhud.discord;

import com.arenzya.discordhud.state.CallState;
import com.arenzya.discordhud.state.ConnectionState;
import com.arenzya.discordhud.state.DiscordSnapshot;
import com.sun.jna.platform.win32.WinDef.HWND;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WindowsDiscordProvider implements DiscordProvider {
    private static final long SCAN_INTERVAL_NANOS = 3_000_000_000L;

    private final Logger logger;
    private final WindowsDiscordController controller;
    private final ExecutorService executor;
    private final AtomicBoolean scanQueued = new AtomicBoolean();

    private volatile HWND discordWindow;
    private volatile boolean discordDetected;
    private volatile long nextScanAt;
    private volatile boolean muted;
    private volatile boolean mutedKnown;
    private volatile boolean deafened;
    private volatile boolean deafenedKnown;
    private volatile boolean screenSharing;
    private volatile boolean screenSharingKnown;

    public WindowsDiscordProvider(Logger logger) {
        this.logger = logger;
        controller = new WindowsDiscordController();
        executor = Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "discord-hud-desktop");
            thread.setDaemon(true);
            return thread;
        });
        refresh();
    }

    @Override
    public DiscordSnapshot snapshot() {
        return new DiscordSnapshot(
                discordDetected ? ConnectionState.CONNECTED : ConnectionState.DISCONNECTED,
                CallState.IDLE,
                "",
                "",
                muted,
                mutedKnown,
                deafened,
                deafenedKnown,
                screenSharing,
                screenSharingKnown,
                0L
        );
    }

    @Override
    public DiscordCapabilities capabilities() {
        return DiscordCapabilities.windowsDesktop();
    }

    @Override
    public void tick() {
        if (System.nanoTime() >= nextScanAt) queueScan();
    }

    @Override
    public boolean acceptCall() {
        return queueAction("answer call", window -> controller.acceptCall(window), null);
    }

    @Override
    public boolean declineCall() {
        return queueAction("decline call", window -> controller.declineCall(window), null);
    }

    @Override
    public boolean toggleMute() {
        return queueAction("toggle mute", window -> controller.toggleMute(), () -> {
            muted = !muted;
            mutedKnown = true;
        });
    }

    @Override
    public boolean toggleDeafen() {
        return queueAction("toggle deafen", window -> controller.toggleDeafen(), () -> {
            deafened = !deafened;
            deafenedKnown = true;
        });
    }

    @Override
    public boolean toggleScreenShare() {
        return queueAction("toggle screen share", window -> controller.toggleScreenShare(), () -> {
            screenSharing = !screenSharing;
            screenSharingKnown = true;
        });
    }

    @Override
    public void refresh() {
        nextScanAt = 0L;
        queueScan();
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    private void queueScan() {
        if (!scanQueued.compareAndSet(false, true)) return;
        nextScanAt = System.nanoTime() + SCAN_INTERVAL_NANOS;

        executor.execute(() -> {
            try {
                HWND found = controller.findDiscordWindow();
                discordWindow = found;
                discordDetected = controller.isUsable(found);
            } catch (RuntimeException e) {
                discordWindow = null;
                discordDetected = false;
                logger.debug("Discord desktop scan failed", e);
            } finally {
                scanQueued.set(false);
            }
        });
    }

    private boolean queueAction(String name, WindowAction action, Runnable onSuccess) {
        executor.execute(() -> {
            try {
                HWND window = discordWindow;
                if (!controller.isUsable(window)) {
                    window = controller.findDiscordWindow();
                    discordWindow = window;
                    discordDetected = controller.isUsable(window);
                }

                if (!discordDetected) {
                    logger.debug("Cannot {} because Discord desktop was not found", name);
                    return;
                }

                if (action.run(window)) {
                    if (onSuccess != null) onSuccess.run();
                } else {
                    logger.debug("Discord desktop action did not complete: {}", name);
                }
            } catch (RuntimeException e) {
                logger.warn("Discord desktop action failed: {}", name, e);
                refresh();
            }
        });
        return true;
    }

    @FunctionalInterface
    private interface WindowAction {
        boolean run(HWND window);
    }
}

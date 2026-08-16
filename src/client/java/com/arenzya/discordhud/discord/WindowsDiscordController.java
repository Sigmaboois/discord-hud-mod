package com.arenzya.discordhud.discord;

import com.sun.jna.Native;
import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import java.util.List;
import java.util.Locale;

final class WindowsDiscordController {
    private interface WindowStateApi extends StdCallLibrary {
        WindowStateApi INSTANCE = Native.load("user32", WindowStateApi.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean IsIconic(HWND window);
    }

    private static final List<String> DISCORD_EXECUTABLES = List.of(
            "discord.exe",
            "discordptb.exe",
            "discordcanary.exe",
            "discorddevelopment.exe"
    );

    private static final int VK_SHIFT = 0x10;
    private static final int VK_CONTROL = 0x11;
    private static final int VK_ALT = 0x12;
    private static final int VK_ENTER = 0x0D;
    private static final int VK_ESCAPE = 0x1B;
    private static final int VK_D = 0x44;
    private static final int VK_M = 0x4D;
    private static final int VK_S = 0x53;

    HWND findDiscordWindow() {
        HWND fallback = null;

        for (DesktopWindow window : WindowUtils.getAllWindows(false)) {
            if (!isDiscordExecutable(window.getFilePath()) && !isDiscordProcess(window.getHWND())) continue;
            if (fallback == null) fallback = window.getHWND();
            if (window.getTitle() != null && !window.getTitle().isBlank()) return window.getHWND();
        }

        return fallback;
    }

    boolean isUsable(HWND window) {
        return window != null && User32.INSTANCE.IsWindow(window);
    }

    boolean toggleMute() {
        return sendToDiscord(findDiscordWindow(), VK_CONTROL, VK_SHIFT, VK_M);
    }

    boolean toggleDeafen() {
        return sendToDiscord(findDiscordWindow(), VK_CONTROL, VK_SHIFT, VK_D);
    }

    boolean toggleScreenShare() {
        return sendToDiscord(findDiscordWindow(), VK_CONTROL, VK_SHIFT, VK_ALT, VK_S);
    }

    boolean acceptCall(HWND discordWindow) {
        return sendToDiscord(discordWindow, VK_CONTROL, VK_ENTER);
    }

    boolean declineCall(HWND discordWindow) {
        return sendToDiscord(discordWindow, VK_ESCAPE);
    }

    private boolean sendToDiscord(HWND discordWindow, int... keys) {
        if (!isUsable(discordWindow)) return false;

        User32 user32 = User32.INSTANCE;
        HWND previous = user32.GetForegroundWindow();
        boolean minimized = WindowStateApi.INSTANCE.IsIconic(discordWindow);

        try {
            if (minimized) user32.ShowWindow(discordWindow, WinUser.SW_RESTORE);
            if (!user32.SetForegroundWindow(discordWindow)) return false;

            sleep(90L);
            HWND foreground = user32.GetForegroundWindow();
            if (foreground == null || !foreground.getPointer().equals(discordWindow.getPointer())) return false;

            boolean sent = sendChord(keys);
            sleep(45L);
            return sent;
        } finally {
            if (previous != null && user32.IsWindow(previous)) {
                user32.SetForegroundWindow(previous);
            }
            if (minimized) {
                user32.ShowWindow(discordWindow, WinUser.SW_MINIMIZE);
            }
        }
    }


    private boolean sendChord(int... keys) {
        int pressed = 0;

        for (int key : keys) {
            if (!sendKey(key, false)) {
                releasePressed(keys, pressed);
                return false;
            }
            pressed++;
        }

        boolean success = true;
        for (int i = keys.length - 1; i >= 0; i--) {
            success &= sendKey(keys[i], true);
        }
        return success;
    }

    private void releasePressed(int[] keys, int pressed) {
        for (int i = pressed - 1; i >= 0; i--) {
            sendKey(keys[i], true);
        }
    }

    private boolean sendKey(int virtualKey, boolean keyUp) {
        WinUser.INPUT input = new WinUser.INPUT();
        input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        input.input.setType(WinUser.KEYBDINPUT.class);

        WinUser.KEYBDINPUT keyboard = new WinUser.KEYBDINPUT();
        keyboard.wVk = new WinDef.WORD(virtualKey);
        keyboard.wScan = new WinDef.WORD(0);
        keyboard.dwFlags = new WinDef.DWORD(keyUp ? WinUser.KEYBDINPUT.KEYEVENTF_KEYUP : 0);
        keyboard.time = new WinDef.DWORD(0);
        keyboard.dwExtraInfo = new BaseTSD.ULONG_PTR(0);

        input.input.ki = keyboard;
        input.write();

        WinDef.DWORD sent = User32.INSTANCE.SendInput(
                new WinDef.DWORD(1),
                new WinUser.INPUT[]{input},
                input.size()
        );
        return sent.intValue() == 1;
    }


    private boolean isDiscordProcess(HWND hwnd) {
        try {
            IntByReference pid = new IntByReference();
            User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);
            long processId = Integer.toUnsignedLong(pid.getValue());
            if (processId == 0) return false;

            return ProcessHandle.of(processId)
                    .flatMap(handle -> handle.info().command())
                    .map(WindowsDiscordController::isDiscordExecutable)
                    .orElse(false);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static boolean isDiscordExecutable(String path) {
        if (path == null || path.isBlank()) return false;
        String normalized = path.replace('\\', '/').toLowerCase(Locale.ROOT);
        int slash = normalized.lastIndexOf('/');
        String fileName = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        return DISCORD_EXECUTABLES.contains(fileName);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

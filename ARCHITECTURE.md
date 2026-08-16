# Architecture

Discord HUD is intentionally a single-process Minecraft client mod.

## Runtime

```text
Minecraft Fabric client
        |
        +-- HUD / settings / keybinds
        |
        +-- DiscordProvider
                |
                +-- WindowsDiscordProvider
                        |
                        +-- JNA window discovery/focus
                        +-- java.awt.Robot keyboard input
```

There is no TCP listener, localhost bridge, companion executable, Discord token, or Discord account session in the mod.

## Windows integration

The provider periodically looks for a top-level window belonging to Discord.exe, DiscordPTB.exe, DiscordCanary.exe, or DiscordDevelopment.exe. Scans and desktop actions run on one daemon worker thread so Minecraft's render/client thread is never blocked by native window enumeration or short focus transitions.

Mute and deafen use Discord's Windows keyboard shortcuts. Answer and decline require Discord to be focused, so the provider remembers the current foreground window, focuses Discord, sends the shortcut, and restores the original window. If Discord was minimized it is restored only for the action and minimized again afterward.

Screen share uses a user-created Discord custom keybind set to Ctrl+Shift+Alt+S.

## Dependency packaging

JNA and JNA Platform are declared with Fabric Loom's `include` configuration. Loom packages them as nested JARs inside the release JAR, keeping installation to a single file while avoiding source shading/relocation.

## Failure behavior

If Windows integration cannot initialize, or if Discord cannot be found, the provider reports an unavailable/disconnected state. Discord functionality must not crash Minecraft.

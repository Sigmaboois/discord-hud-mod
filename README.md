# Discord HUD

Discord HUD is a Fabric client mod for Minecraft 1.21.11 that exposes a small set of Discord desktop controls without requiring a second application.

## What it does

On Windows, the mod can:

- toggle Discord mute
- toggle Discord deafen
- answer an incoming Discord call
- decline an incoming Discord call
- trigger a Discord Toggle Screen Share custom keybind
- show a compact in-game status HUD
- provide configurable Minecraft keybinds and a minimal settings screen

The mod does not use Discord user tokens, self-bots, browser cookies, or account automation.

## Single-JAR design

The release is one Fabric mod JAR. The Windows native-access libraries are packaged as Fabric nested JARs, so users do not install an EXE or a separate library.

Discord HUD looks for the desktop Discord process locally. Answer and decline briefly give Discord keyboard focus, send Discord's documented shortcut, then restore the previous foreground window. Mute, deafen, and the screen-share keybind are sent as desktop keyboard shortcuts.

## Current limitation

Discord does not provide this mod a supported API for reading the normal desktop client's caller identity or exact voice state. The incoming call card is therefore a UI preview rather than automatic caller detection. Mute/deafen indicators become known after those controls are used through Discord HUD and can become out of sync if the same state is changed manually in Discord.

## Screen share setup

In Discord desktop, add a custom keybind:

`Toggle Screen Share` -> `Ctrl + Shift + Alt + S`

Discord HUD sends that chord when its Minecraft screen-share keybind is pressed.

## Requirements

- Minecraft 1.21.11
- Fabric Loader
- Fabric API
- Java 21
- Windows for Discord desktop controls
- Mod Menu is optional

## Build

```bash
gradle clean build
```

The release JAR is produced under `build/libs/`.

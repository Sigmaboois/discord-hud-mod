# Testing Discord HUD 1.1.2

## Installation smoke test

1. Use Minecraft 1.21.11 with Fabric Loader, Fabric API, and Java 21.
2. Put only `discord-hud-1.1.2.jar` in the mods folder for Discord HUD. No companion app is required.
3. Start Discord desktop, then launch Minecraft.
4. Open Discord HUD settings with the configured keybind (O by default).
5. Confirm the status changes to DISCORD after detection.

## UI checks

- Toggle the compact HUD.
- Toggle privacy mode and reduced motion.
- Change HUD opacity.
- Use Preview call and verify the popup animates correctly.
- Open and close the settings screen repeatedly.
- Test multiple Minecraft GUI scales and fullscreen/windowed mode.

## Discord control checks

### Mute and deafen

1. Join a Discord voice channel.
2. Keep Minecraft focused.
3. Press the Minecraft Discord HUD mute bind.
4. Confirm Discord toggles mute.
5. Repeat for deafen.

### Answer and decline

1. Have another account call the Discord account.
2. Keep Minecraft focused.
3. Press the Discord HUD answer bind and confirm the call is answered and focus returns to Minecraft.
4. Repeat with a new call and the decline bind.

### Screen share

1. In Discord desktop, set `Toggle Screen Share` to `Ctrl+Shift+Alt+S` under User Settings > Keybinds.
2. Join a voice channel/call where screen sharing is available.
3. Press the Discord HUD screen-share bind.
4. Confirm Discord performs the configured screen-share action.

## Failure checks

- Close Discord and use Refresh; status should become NOT FOUND/OFFLINE.
- Press Discord control binds while Discord is closed; Minecraft must not crash or freeze.
- Restart Discord, use Refresh, and confirm detection returns.
- Change mute/deafen manually in Discord and note that HUD state can become out of sync until those toggles are used again through the mod.

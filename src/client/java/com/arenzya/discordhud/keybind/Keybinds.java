package com.arenzya.discordhud.keybind;

import com.arenzya.discordhud.DiscordHudClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class Keybinds {
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(DiscordHudClient.MOD_ID, "controls")
    );

    private final DiscordHudClient mod;

    private final KeyMapping openSettings = key("key.discordhud.open_settings", InputConstants.KEY_O);
    private final KeyMapping acceptCall = key("key.discordhud.accept_call", InputConstants.UNKNOWN.getValue());
    private final KeyMapping declineCall = key("key.discordhud.decline_call", InputConstants.UNKNOWN.getValue());
    private final KeyMapping toggleMute = key("key.discordhud.toggle_mute", InputConstants.UNKNOWN.getValue());
    private final KeyMapping toggleDeafen = key("key.discordhud.toggle_deafen", InputConstants.UNKNOWN.getValue());
    private final KeyMapping toggleScreenShare = key("key.discordhud.toggle_screen_share", InputConstants.UNKNOWN.getValue());
    private final KeyMapping toggleHud = key("key.discordhud.toggle_hud", InputConstants.UNKNOWN.getValue());

    public Keybinds(DiscordHudClient mod) {
        this.mod = mod;
    }

    public void register() {
        KeyBindingHelper.registerKeyBinding(openSettings);
        KeyBindingHelper.registerKeyBinding(acceptCall);
        KeyBindingHelper.registerKeyBinding(declineCall);
        KeyBindingHelper.registerKeyBinding(toggleMute);
        KeyBindingHelper.registerKeyBinding(toggleDeafen);
        KeyBindingHelper.registerKeyBinding(toggleScreenShare);
        KeyBindingHelper.registerKeyBinding(toggleHud);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openSettings.consumeClick()) mod.openSettings();
            while (acceptCall.consumeClick()) mod.acceptCall();
            while (declineCall.consumeClick()) mod.declineCall();
            while (toggleMute.consumeClick()) mod.provider().toggleMute();
            while (toggleDeafen.consumeClick()) mod.provider().toggleDeafen();
            while (toggleScreenShare.consumeClick()) mod.provider().toggleScreenShare();
            while (toggleHud.consumeClick()) {
                mod.config().get().hudEnabled = !mod.config().get().hudEnabled;
                mod.config().save();
            }
            mod.provider().tick();
        });
    }

    private static KeyMapping key(String translation, int keyCode) {
        return new KeyMapping(translation, InputConstants.Type.KEYSYM, keyCode, CATEGORY);
    }
}

package com.arenzya.discordhud.ui;

import com.arenzya.discordhud.DiscordHudClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public final class DiscordHudModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new DiscordHudSettingsScreen(DiscordHudClient.instance(), parent);
    }
}

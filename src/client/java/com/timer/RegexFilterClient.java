package com.timer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import java.util.regex.Pattern;

public class RegexFilterClient implements ClientModInitializer, ModMenuApi {
    @Override
    public void onInitializeClient() {
        ModConfig.load();

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, isActionBar) -> {
            if (!ModConfig.getInstance().enabled) return true;

            String rawMessage = message.getString();
            for (String regex : ModConfig.getInstance().regexFilters) {
                if (Pattern.compile(regex).matcher(rawMessage).find()) {
                    return false;
                }
            }
            return true;
        });
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModConfigScreen::createConfigScreen;
    }
}
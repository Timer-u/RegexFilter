package com.timer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexFilterClient implements ClientModInitializer, ModMenuApi {
    private static final Logger LOGGER = LogManager.getLogger("RegexFilter");
    
    @Override
    public void onInitializeClient() {
        ModConfig.load();

        ClientReceiveMessageEvents.ALLOW_GAME.register((message, isActionBar) -> {
            if (!ModConfig.getInstance().enabled) return true;

            String rawMessage = message.getString();
            for (String regex : ModConfig.getInstance().regexFilters) {
                try {
                    if (Pattern.compile(regex).matcher(rawMessage).find()) {
                        LOGGER.debug("[Filter] Blocked message matching '{}': {}", regex, rawMessage);
                        return false;
                    }
                } catch (PatternSyntaxException e) {
                    LOGGER.error("[Filter] Invalid regex pattern: {}", regex, e);
                }
            }
            return true;
        });
        LOGGER.info("RegexFilter initialized");
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModConfigScreen::createConfigScreen;
    }
}

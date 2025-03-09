package com.timer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RegexFilterClient implements ClientModInitializer, ModMenuApi {
    private static final Logger LOGGER = LogManager.getLogger("RegexFilter");

    @Override
    public void onInitializeClient() {
        // 设置日志目录系统属性
        Path logDir = FabricLoader.getInstance().getGameDir().resolve("logs");
        System.setProperty("regexfilter.logdir", logDir.toString());

        ModConfig.load();

        ClientReceiveMessageEvents.ALLOW_GAME.register(
                (message, isActionBar) -> {
                    if (!ModConfig.getInstance().enabled) return true;

                    String rawMessage = message.getString();
                    List<String> regexList = ModConfig.getInstance().regexFilters;

                    // 空列表直接放行
                    if (regexList == null || regexList.isEmpty()) {
                        return true;
                    }

                    for (String regex : regexList) {
                        try {
                            if (Pattern.compile(regex).matcher(rawMessage).find()) {
                                LOGGER.debug(
                                        "[Filter] Blocked message matching '{}': {}",
                                        regex,
                                        rawMessage);
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

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
import net.minecraft.text.Text;
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
                    List<Pattern> patterns = ModConfig.getInstance().getCompiledPatterns();

                    if (patterns.isEmpty()) {
                        return true;
                    }

                    for (Pattern pattern : patterns) {
                        if (pattern.matcher(rawMessage).find()) {
                            LOGGER.debug("[Filter] Blocked message matching '{}': {}",
                                    pattern.pattern(), rawMessage);
                            return false;
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

    // 测试用方法
    static boolean shouldAllowMessage(Text message) {
        if (!ModConfig.getInstance().enabled) return true;

        String rawMessage = message.getString();
        List<Pattern> patterns = ModConfig.getInstance().getCompiledPatterns();

        if (patterns.isEmpty()) {
            return true;
        }

        for (Pattern pattern : patterns) {
            if (pattern.matcher(rawMessage).find()) {
                return false;
            }
        }
        return true;
    }
}

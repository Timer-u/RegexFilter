package com.timer;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import java.util.regex.Pattern;

public class RegexFilterClient implements ClientModInitializer, ModMenuApi {
    @Override
    public void onInitializeClient() {
        // 初始化时加载配置文件
        ModConfig.load();

        // 监听聊天消息并过滤
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, isActionBar) -> {
            if (!ModConfig.enabled) return true;

            String rawMessage = message.getString();
            for (String regex : ModConfig.regexFilters) {
                if (Pattern.compile(regex).matcher(rawMessage).find()) {
                    return false; // 拦截消息
                }
            }
            return true;
        });
    }

    // ModMenu 接口：返回配置界面
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModConfigScreen::createConfigScreen;
    }
}
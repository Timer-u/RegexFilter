package com.timer;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.ArrayList;

public class ModConfigScreen {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.regexfilter.config"))
                .setSavingRunnable(ModConfig::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.general"));

        // 启用模组开关
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.enabled"), ModConfig.getInstance().enabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> ModConfig.getInstance().enabled = newValue)
                .build());

        // 正则表达式列表（带悬浮提示）
        general.addEntry(entryBuilder.startStrList(Text.translatable("option.regex_list"), ModConfig.getInstance().regexFilters)
                .setDefaultValue(new ArrayList<>())
                .setSaveConsumer(newList -> ModConfig.getInstance().regexFilters = new ArrayList<>(newList))
                .setTooltip(
                    Text.translatable("tooltip.regex_list.1"),
                    Text.translatable("tooltip.regex_list.2")
                )
                .build());

        return builder.build();
    }
}

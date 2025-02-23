package com.timer;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModConfigScreen {
    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.regexfilter.config"))
                .setSavingRunnable(() -> {
                    // 保存配置到文件
                    new ModConfig().save();
                });

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.general"));

        // 总开关配置项
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.enabled"), ModConfig.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> ModConfig.enabled = newValue)
                .build());

        // 正则列表配置项
        general.addEntry(entryBuilder.startStrList(Text.translatable("option.regex_list"), ModConfig.regexFilters)
                .setDefaultValue(new ArrayList<>())
                .setSaveConsumer(newList -> ModConfig.regexFilters = newList)
                .build());

        return builder.build();
    }
}
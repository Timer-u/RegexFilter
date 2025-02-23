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
                .setSavingRunnable(() -> ModConfig.save()); // 直接调用静态保存方法

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
                .setSaveConsumer(newList -> ModConfig.regexFilters = new ArrayList<>(newList)) // 深拷贝
                .build());

        return builder.build();
    }
}
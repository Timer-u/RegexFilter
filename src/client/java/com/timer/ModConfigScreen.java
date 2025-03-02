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

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.enabled"), ModConfig.getInstance().enabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> ModConfig.getInstance().enabled = newValue)
                .build());

        general.addEntry(entryBuilder.startStrList(Text.translatable("option.regex_list"), ModConfig.getInstance().regexFilters)
                .setDefaultValue(new ArrayList<>())
                .setSaveConsumer(newList -> ModConfig.getInstance().regexFilters = new ArrayList<>(newList))
                .build());

        return builder.build();
    }
}

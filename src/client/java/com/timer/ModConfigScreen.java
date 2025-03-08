package com.timer;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ModConfigScreen {
    private static final boolean DEFAULT_ENABLED = true;

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.regexfilter.config"))
                .setSavingRunnable(ModConfig::save)
                .setDefaultBackgroundTexture(Identifier.of("minecraft", "textures/block/stone.png"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.general"));

        // 启用开关
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.enabled"), ModConfig.getInstance().enabled)
                .setDefaultValue(DEFAULT_ENABLED)
                .setSaveConsumer(newValue -> ModConfig.getInstance().enabled = newValue)
                .setYesNoTextSupplier(value -> Text.translatable(value ? "text.cloth-config.on" : "text.cloth-config.off"))
                .requireRestart()
                .build());

        // 正则表达式列表
        general.addEntry(entryBuilder.startStrList(Text.translatable("option.regex_list"), 
                    ModConfig.getInstance().regexFilters)
                .setDefaultValue(Collections.emptyList()) // 设置为空默认列表
                .setInsertButtonEnabled(true)
                .setDeleteButtonEnabled(true)
                .setCellErrorSupplier(value -> {
                    if (value != null && !value.isEmpty()) {
                        try {
                            Pattern.compile(value);
                            return Optional.empty();
                        } catch (PatternSyntaxException e) {
                            return Optional.of(Text.translatable("error.invalid_regex"));
                        }
                    }
                    return Optional.empty();
                })
                .setSaveConsumer(newList -> {
                    newList.removeIf(str -> str == null || str.trim().isEmpty());
                    ModConfig.getInstance().regexFilters = newList;
                })
                .requireRestart()
                .build());

        return builder.build();
    }
}

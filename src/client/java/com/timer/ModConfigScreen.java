package com.timer;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ModConfigScreen {
    // 定义与ModConfig一致的默认配置
    private static final boolean DEFAULT_ENABLED = true;
    private static final ArrayList<String> DEFAULT_REGEX = new ArrayList<>(Collections.singletonList("^\\[系统\\].*"));

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.regexfilter.config"))
                .setSavingRunnable(ModConfig::save)
                .setDefaultBackgroundTexture(new Identifier("textures/block/stone.png"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.general"));

        // 启用模组开关
        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("option.enabled"), ModConfig.getInstance().enabled)
                .setDefaultValue(DEFAULT_ENABLED)
                .setSaveConsumer(newValue -> ModConfig.getInstance().enabled = newValue)
                .setTooltip(Text.translatable("tooltip.enabled"))
                .build());

        // 正则表达式列表优化
        general.addEntry(entryBuilder.startStringList(Text.translatable("option.regex_list"), 
                    ModConfig.getInstance().regexFilters)
                .setDefaultValue(DEFAULT_REGEX)
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
                    ModConfig.getInstance().regexFilters = new ArrayList<>(newList);
                })
                .setTooltip(
                    Text.translatable("tooltip.regex_list.1"),
                    Text.translatable("tooltip.regex_list.2"),
                    Text.translatable("tooltip.regex_list.3")
                )
                .build());

        // 重置按钮配置
        builder.setDefaultButtonSupplier(() -> new ButtonWidget.Builder(Text.translatable("button.reset_defaults"), button -> {
                    ModConfig.getInstance().enabled = DEFAULT_ENABLED;
                    ModConfig.getInstance().regexFilters = new ArrayList<>(DEFAULT_REGEX);
                    ModConfig.save();
                    MinecraftClient.getInstance().setScreen(createConfigScreen(parent));
                })
                .size(100, 20)
                .build());

        return builder.build();
    }
}

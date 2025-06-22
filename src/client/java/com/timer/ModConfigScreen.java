package com.timer;

import net.minecraft.text.Text;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Identifier;

public class ModConfigScreen {
    private static final boolean DEFAULT_ENABLED = true;

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder =
                ConfigBuilder.create()
                        .setParentScreen(parent)
                        .setTitle(Text.translatable("title.regexfilter.config"))
                        .setSavingRunnable(ModConfig::save)
                        .setDefaultBackgroundTexture(
                                Identifier.of("minecraft", "textures/block/stone.png"))
                        .setDoesConfirmSave(true);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.general"));

        // 启用开关
        general.addEntry(
                entryBuilder
                        .startBooleanToggle(
                                Text.translatable("option.enabled"),
                                ModConfig.getInstance().enabled)
                        .setDefaultValue(DEFAULT_ENABLED)
                        .setSaveConsumer(newValue -> ModConfig.getInstance().enabled = newValue)
                        // 保持原有Cloth Config本地化键
                        .setYesNoTextSupplier(
                                value ->
                                        Text.translatable(
                                                value
                                                        ? "text.cloth-config.on"
                                                        : "text.cloth-config.off"))
                        .setTooltip(Text.translatable("tooltip.enabled")) // 还原为原始单条提示
                        .requireRestart()
                        .build());

        // 正则表达式列表
        general.addEntry(
                entryBuilder
                        .startStrList(
                                Text.translatable("option.regex_list"),
                                // 使用 CopyOnWriteArrayList 包装当前配置值
                                new CopyOnWriteArrayList<>(
                                        ModConfig.getInstance().getRegexFilters()))
                        .setDefaultValue(Collections.emptyList())
                        .setInsertButtonEnabled(true) // 允许插入按钮
                        .setDeleteButtonEnabled(true) // 允许删除按钮
                        // 实时验证正则表达式有效性
                        .setCellErrorSupplier(
                                value -> {
                                    if (value == null || value.isEmpty()) {
                                        return Optional.empty();
                                    }
                                    try {
                                        Pattern.compile(value);
                                        return Optional.empty();
                                    } catch (PatternSyntaxException e) {
                                        return Optional.of(
                                                Text.translatable(
                                                        "error.invalid_regex.detail",
                                                        e.getDescription()));
                                    }
                                })
                        // 保存时自动清理空值和无效正则
                        .setSaveConsumer(
                                newList -> {
                                    // 清理空值和空白字符串
                                    newList.removeIf(str -> str == null || str.trim().isEmpty());
                                    // 使用线程安全集合更新配置
                                    ModConfig.getInstance()
                                            .setRegexFilters(new CopyOnWriteArrayList<>(newList));
                                })
                        // 工具提示配置
                        .setTooltip(
                                Text.translatable("tooltip.regex_list.1"),
                                Text.translatable("tooltip.regex_list.2"))
                        .requireRestart()
                        .build());
        return builder.build();
    }
}

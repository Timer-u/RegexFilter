package com.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModConfig {
    private static final Logger LOGGER = LogManager.getLogger("RegexFilter");

    // 实例字段
    public boolean enabled = true;
    public List<String> regexFilters = new ArrayList<>();

    // 单例管理
    private static ModConfig INSTANCE = new ModConfig();

    public static ModConfig getInstance() {
        return INSTANCE;
    }

    private static Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("regexfilter.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 添加静态方法用于测试环境设置路径
    public static void setConfigPathForTest(Path path) {
        CONFIG_PATH = path;
    }

    // 预编译的正则表达式缓存
    private transient List<Pattern> compiledPatterns = new ArrayList<>();

    // 更新预编译的正则表达式
    void updateCompiledPatterns() {
        compiledPatterns.clear();
        if (regexFilters == null) return;

        for (String regex : regexFilters) {
            try {
                compiledPatterns.add(Pattern.compile(regex));
            } catch (PatternSyntaxException e) {
                LOGGER.warn("Skipping invalid pattern during compilation: {}", regex);
            }
        }
    }

    // 加载配置
    public static void load() {
        LOGGER.info("Loading config...");
        try {
            if (!Files.exists(CONFIG_PATH)) {
                LOGGER.info("Creating default config");
                Files.createDirectories(CONFIG_PATH.getParent()); // 确保目录存在
                INSTANCE = new ModConfig();
                save(); // 调用 save() 创建默认文件
                return;
            }

            // 使用 BufferedReader 读取文件
            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);

                // 处理 loaded 为 null 的情况
                if (loaded == null) {
                    LOGGER.error("Config file is invalid, using default configuration");
                    loaded = new ModConfig();
                }

                // 确保 regexFilters 不为 null
                if (loaded.regexFilters == null) {
                    loaded.regexFilters = new ArrayList<>(INSTANCE.regexFilters);
                } else {
                    // 使用 Stream 过滤空或 null 的条目
                    loaded.regexFilters =
                            loaded.regexFilters.stream()
                                    .filter(str -> str != null && !str.trim().isEmpty())
                                    .collect(Collectors.toCollection(ArrayList::new));
                }

                INSTANCE = loaded;
            }
        } catch (IOException e) {
            LOGGER.error("IO Error loading config: {}", e.getMessage());
            INSTANCE = new ModConfig();
        } catch (JsonSyntaxException e) {
            LOGGER.error("Invalid JSON syntax: {}", e.getMessage());
            INSTANCE = new ModConfig();
        } finally {
            INSTANCE.updateCompiledPatterns(); // 始终更新预编译正则表达式
            LOGGER.info("Loaded {} valid regex patterns", INSTANCE.compiledPatterns.size());
        }
    }

    // 保存配置
    public static void save() {
        // 清理无效的正则表达式
        List<String> cleanList =
                INSTANCE.regexFilters.stream()
                        .filter(str -> str != null && !str.trim().isEmpty())
                        .filter(
                                str -> {
                                    try {
                                        Pattern.compile(str); // 仅用于验证，不重复编译
                                        return true;
                                    } catch (PatternSyntaxException e) {
                                        LOGGER.warn("Removing invalid pattern: {}", str);
                                        return false;
                                    }
                                })
                        .collect(Collectors.toList());

        INSTANCE.regexFilters = cleanList;
        INSTANCE.updateCompiledPatterns(); // 保存前更新缓存

        try {
            Files.createDirectories(CONFIG_PATH.getParent()); // 确保目录存在
            String json = GSON.toJson(INSTANCE);
            Files.writeString(CONFIG_PATH, json);
            LOGGER.info("Config saved with {} patterns", INSTANCE.compiledPatterns.size());
        } catch (IOException e) {
            LOGGER.error("Config save failed", e);
        }
    }

    // 获取只读的预编译正则列表
    public List<Pattern> getCompiledPatterns() {
        return compiledPatterns.stream()
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(), Collections::unmodifiableList));
    }
}

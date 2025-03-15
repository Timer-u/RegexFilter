package com.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
                INSTANCE = new ModConfig();
                save();
                return;
            }
    
            String json = Files.readString(CONFIG_PATH);
            ModConfig loaded = GSON.fromJson(json, ModConfig.class);
    
            // 确保 regexFilters 不为 null
            if (loaded.regexFilters == null) {
                loaded.regexFilters = new ArrayList<>(INSTANCE.regexFilters);
            } else {
                loaded.regexFilters = new ArrayList<>(loaded.regexFilters);
                loaded.regexFilters.removeIf(str -> str == null || str.trim().isEmpty());
            }
    
            // 确保其他字段的默认值
            if (loaded.enabled == null) {
                loaded.enabled = true; // 如果使用 Boolean 类型，否则保持原逻辑
            }
    
            INSTANCE = loaded;
            INSTANCE.updateCompiledPatterns();
            LOGGER.info("Loaded {} valid regex patterns", INSTANCE.compiledPatterns.size());
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Config load failed", e);
            INSTANCE = new ModConfig();
            INSTANCE.updateCompiledPatterns();
        }
    }

    // 保存配置
    public static void save() {
        // 清理无效的正则表达式
        List<String> cleanList = new ArrayList<>(INSTANCE.regexFilters);
        cleanList.removeIf(str -> {
            if (str == null || str.trim().isEmpty()) return true;
            try {
                Pattern.compile(str); // 仅用于验证，不重复编译
                return false;
            } catch (PatternSyntaxException e) {
                LOGGER.warn("Removing invalid pattern: {}", str);
                return true;
            }
        });

        INSTANCE.regexFilters = cleanList;
        INSTANCE.updateCompiledPatterns(); // 保存前更新缓存

        try {
            String json = GSON.toJson(INSTANCE);
            Files.writeString(CONFIG_PATH, json);
            LOGGER.info("Config saved with {} patterns", INSTANCE.compiledPatterns.size());
        } catch (IOException e) {
            LOGGER.error("Config save failed", e);
        }
    }

    // 获取只读的预编译正则列表
    public List<Pattern> getCompiledPatterns() {
        return Collections.unmodifiableList(compiledPatterns);
    }
}

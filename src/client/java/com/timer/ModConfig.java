package com.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModConfig {
    private static final Logger LOGGER = LogManager.getLogger("RegexFilter");
    
    // 实例字段
    public boolean enabled = true;
    public List<String> regexFilters = new ArrayList<>();
    
    // 单例管理
    private static ModConfig INSTANCE = new ModConfig();
    public static ModConfig getInstance() { return INSTANCE; }
    
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("regexfilter.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 加载配置（增强null值处理）
    public static void load() {
        LOGGER.info("Loading config...");
        try {
            if (!Files.exists(CONFIG_PATH)) {
                LOGGER.info("Creating default config");
                INSTANCE = new ModConfig(); // 使用类默认值
                save();
                return;
            }
            
            String json = Files.readString(CONFIG_PATH);
            ModConfig loaded = GSON.fromJson(json, ModConfig.class);
            
            // 字段合法性校验
            if (loaded.regexFilters == null) {
                loaded.regexFilters = new ArrayList<>(INSTANCE.regexFilters); // 恢复默认
            } else {
                loaded.regexFilters.removeIf(str -> str == null || str.trim().isEmpty());
            }
            
            INSTANCE = loaded;
            LOGGER.info("Loaded {} valid regex patterns", INSTANCE.regexFilters.size());
        } catch (IOException e) {
            LOGGER.error("Config load failed", e);
            INSTANCE = new ModConfig(); // 故障时回退默认
        }
    }

    // 保存配置（优化日志输出）
    public static void save() {
        // 执行数据清洗
        List<String> cleanList = new ArrayList<>(INSTANCE.regexFilters);
        cleanList.removeIf(str -> str == null || str.trim().isEmpty());
        INSTANCE.regexFilters = cleanList;

        LOGGER.debug("Saving config with {} patterns", cleanList.size());
        try {
            String json = GSON.toJson(INSTANCE);
            Files.writeString(CONFIG_PATH, json);
            LOGGER.info("Config saved to {}", CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}

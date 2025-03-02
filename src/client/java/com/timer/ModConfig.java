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
import java.util.List;

public class ModConfig {
    // 日志记录器
    private static final Logger LOGGER = LogManager.getLogger("RegexFilter");
    
    // 实例字段
    public boolean enabled = true;
    public List<String> regexFilters = new ArrayList<>();
    
    // 单例管理
    private static ModConfig INSTANCE = new ModConfig();
    public static ModConfig getInstance() { return INSTANCE; }
    
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("regexfilter.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 加载配置
    public static void load() {
        LOGGER.info("Loading config...");
        try {
            if (!Files.exists(CONFIG_PATH)) {
                LOGGER.info("Config file not found, creating default");
                save();
                return;
            }
            String json = Files.readString(CONFIG_PATH);
            INSTANCE = GSON.fromJson(json, ModConfig.class);
            LOGGER.info("Config loaded successfully");
        } catch (IOException e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    // 保存配置
    public static void save() {
        LOGGER.debug("Saving config...");
        try {
            String json = GSON.toJson(INSTANCE);
            Files.writeString(CONFIG_PATH, json);
            LOGGER.info("Config saved successfully");
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}

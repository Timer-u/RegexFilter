package com.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
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
        try {
            if (!Files.exists(CONFIG_PATH)) {
                save();
                return;
            }
            String json = Files.readString(CONFIG_PATH);
            INSTANCE = GSON.fromJson(json, ModConfig.class); // 覆盖单例
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 保存配置
    public static void save() {
        try {
            String json = GSON.toJson(INSTANCE);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

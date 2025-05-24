package com.timer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
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
    private List<String> regexFilters = new CopyOnWriteArrayList<>();

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
    private transient List<Pattern> compiledPatterns = new CopyOnWriteArrayList<>();

    // 更新预编译的正则表达式
    void updateCompiledPatterns() {
        List<Pattern> newPatterns = new CopyOnWriteArrayList<>();
        if (regexFilters != null) {
            for (String regex : regexFilters) {
                try {
                    // 检查是否已经编译过相同的正则表达式
                    Optional<Pattern> existing =
                            compiledPatterns.stream()
                                    .filter(p -> p.pattern().equals(regex))
                                    .findFirst();

                    if (existing.isPresent()) {
                        newPatterns.add(existing.get()); // 重用已编译的 Pattern
                    } else {
                        newPatterns.add(Pattern.compile(regex));
                    }
                } catch (PatternSyntaxException e) {
                    LOGGER.warn("Skipping invalid pattern during compilation: {}", regex);
                }
            }
        }
        compiledPatterns = newPatterns;
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
                // 反序列化为 ConfigRecord
                ConfigRecord loadedRecord = GSON.fromJson(reader, ConfigRecord.class);

                // 处理可能的空值或无效 JSON
                if (loadedRecord == null) {
                    LOGGER.error("Config file is invalid, using default configuration");
                    loadedRecord = new ConfigRecord(true, new CopyOnWriteArrayList<>());
                }

                // 将 ConfigRecord 数据复制到当前实例
                INSTANCE.enabled = loadedRecord.enabled();
                // 初始化线程安全列表
                INSTANCE.regexFilters = new CopyOnWriteArrayList<>(loadedRecord.regexFilters());

                // 数据清理：过滤空值和无效正则
                INSTANCE.regexFilters =
                        INSTANCE.regexFilters.stream()
                                .filter(str -> str != null && !str.trim().isEmpty())
                                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
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
        // 清理无效正则表达式并更新实例数据
        List<String> cleanList = INSTANCE.regexFilters.stream()
                .filter(str -> str != null && !str.trim().isEmpty()) // 过滤空值和空白字符串
                .filter(str -> {
                    try {
                        Pattern.compile(str); // 验证正则表达式有效性
                        return true;
                    } catch (PatternSyntaxException e) {
                        LOGGER.warn("Removing invalid pattern: {}", str);
                        return false;
                    }
                })
                .collect(Collectors.toList()); // 收集为普通列表
    
        // 使用线程安全集合更新实例的正则列表
        INSTANCE.regexFilters = new CopyOnWriteArrayList<>(cleanList);
        INSTANCE.updateCompiledPatterns(); // 更新预编译正则缓存
    
        // 创建要保存的 ConfigRecord 实例
        ConfigRecord toSave = new ConfigRecord(
                INSTANCE.enabled,
                List.copyOf(INSTANCE.regexFilters) // 生成不可变副本以避免外部修改
        );
    
        // 序列化并写入配置文件
        try {
            Files.createDirectories(CONFIG_PATH.getParent()); // 确保配置目录存在
            String json = GSON.toJson(toSave); // 使用 Gson 序列化
            Files.writeString(CONFIG_PATH, json); // 原子写入操作
            LOGGER.info("Config saved with {} valid patterns", cleanList.size());
        } catch (IOException e) {
            LOGGER.error("Config save failed", e); // 保留原始错误日志
        }
    }

    // 获取只读的预编译正则列表
    public List<Pattern> getCompiledPatterns() {
        return Collections.unmodifiableList(compiledPatterns);
    }

    // 外部访问正则列表时返回不可变副本
    public List<String> getRegexFilters() {
        return List.copyOf(regexFilters);
    }

    // 更新正则列表
    public void setRegexFilters(List<String> newFilters) {
        this.regexFilters = new CopyOnWriteArrayList<>(newFilters);
    }
}

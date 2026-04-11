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

    public static void setConfigPathForTest(Path path) {
        CONFIG_PATH = path;
    }

    private transient List<Pattern> compiledPatterns = new CopyOnWriteArrayList<>();

    void updateCompiledPatterns() {
        List<Pattern> newPatterns = new CopyOnWriteArrayList<>();
        if (regexFilters != null) {
            for (String regex : regexFilters) {
                try {
                    Optional<Pattern> existing =
                            compiledPatterns.stream()
                                    .filter(p -> p.pattern().equals(regex))
                                    .findFirst();

                    if (existing.isPresent()) {
                        newPatterns.add(existing.get());
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

    public void setRegexFilters(List<String> newFilters) {
        this.regexFilters = new CopyOnWriteArrayList<>(newFilters);
        updateCompiledPatterns();
    }

    // 加载配置
    public static void load() {
        LOGGER.info("Loading config...");
        try {
            if (!Files.exists(CONFIG_PATH)) {
                LOGGER.info("Creating default config");
                Files.createDirectories(CONFIG_PATH.getParent());
                INSTANCE = new ModConfig();
                save();
                return;
            }

            try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ConfigRecord loadedRecord = GSON.fromJson(reader, ConfigRecord.class);

                if (loadedRecord == null) {
                    LOGGER.error("Config file is invalid, using default configuration");
                    loadedRecord = new ConfigRecord(true, new CopyOnWriteArrayList<>());
                }

                INSTANCE.enabled = loadedRecord.enabled();
                INSTANCE.regexFilters = new CopyOnWriteArrayList<>(loadedRecord.regexFilters());

                List<String> validRegex =
                        INSTANCE.regexFilters.stream()
                                .filter(str -> str != null && !str.trim().isEmpty())
                                .filter(
                                        str -> {
                                            try {
                                                Pattern.compile(str);
                                                return true;
                                            } catch (PatternSyntaxException e) {
                                                LOGGER.warn(
                                                        "Removing invalid pattern during loading: {}",
                                                        str);
                                                return false;
                                            }
                                        })
                                .collect(Collectors.toList());

                INSTANCE.regexFilters = new CopyOnWriteArrayList<>(validRegex);

                INSTANCE.updateCompiledPatterns();
            }
        } catch (IOException e) {
            LOGGER.error("IO Error loading config: {}", e.getMessage());
            INSTANCE = new ModConfig();
        } catch (JsonSyntaxException e) {
            LOGGER.error("Invalid JSON syntax: {}", e.getMessage());
            INSTANCE = new ModConfig();
        } finally {
            LOGGER.info("Loaded {} valid regex patterns", INSTANCE.compiledPatterns.size());
        }
    }

    // 保存配置
    public static void save() {
        List<String> cleanList =
                INSTANCE.regexFilters.stream()
                        .filter(str -> str != null && !str.trim().isEmpty())
                        .filter(
                                str -> {
                                    try {
                                        Pattern.compile(str);
                                        return true;
                                    } catch (PatternSyntaxException e) {
                                        LOGGER.warn("Removing invalid pattern: {}", str);
                                        return false;
                                    }
                                })
                        .collect(Collectors.toList());

        INSTANCE.setRegexFilters(cleanList);

        ConfigRecord toSave =
                new ConfigRecord(INSTANCE.enabled, List.copyOf(INSTANCE.regexFilters));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(toSave);
            Files.writeString(CONFIG_PATH, json);
            LOGGER.info("Config saved with {} valid patterns", cleanList.size());
        } catch (IOException e) {
            LOGGER.error("Config save failed", e);
        }
    }

    public List<Pattern> getCompiledPatterns() {
        return Collections.unmodifiableList(compiledPatterns);
    }

    public List<String> getRegexFilters() {
        return List.copyOf(regexFilters);
    }
}

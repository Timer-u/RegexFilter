package com.timer;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.*;

public class ModConfigTest {
    private static Path tempConfig;

    @BeforeAll
    static void setup() throws IOException {
        tempConfig = Files.createTempFile("regexfilter-test", ".json");
        ModConfig.setConfigPathForTest(tempConfig);
    }

    @AfterEach
    void reset() {
        ModConfig.load();
    }

    @Test
    void load_shouldHandleMissingFile() {
        tempConfig.toFile().delete();
        ModConfig.load();
        assertThat(ModConfig.getInstance().enabled).isTrue();
        // 获取正则列表
        assertThat(ModConfig.getInstance().getRegexFilters()).isEmpty();
    }

    @Test
    void save_shouldCleanInvalidRegex() {
        ModConfig.getInstance().setRegexFilters(List.of("valid.*", "[invalid"));
        ModConfig.save();
        // 验证清理后的列表
        assertThat(ModConfig.getInstance().getRegexFilters()).containsExactly("valid.*");
    }

    @Test
    void load_shouldHandleInvalidJson() throws IOException {
        Files.writeString(tempConfig, "{ invalid json }");
        ModConfig.load();
        assertThat(ModConfig.getInstance().enabled).isTrue();
    }

    @Test
    void shouldCompileValidPatterns() {
        ModConfig.getInstance().setRegexFilters(List.of("valid.*", "[a-z]+"));
        ModConfig.getInstance().updateCompiledPatterns();
        List<Pattern> compiled = ModConfig.getInstance().getCompiledPatterns();
        assertThat(compiled).hasSize(2);
        assertThat(compiled.get(0).pattern()).isEqualTo("valid.*");
    }

    @Test
    void shouldSkipInvalidPatterns() {
        ModConfig.getInstance().setRegexFilters(List.of("valid.*", "[invalid["));
        ModConfig.getInstance().updateCompiledPatterns();
        List<Pattern> compiled = ModConfig.getInstance().getCompiledPatterns();
        assertThat(compiled).hasSize(1);
    }
}

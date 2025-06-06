package com.timer;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.*;
import java.util.ArrayList;

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
        // 设置包含无效正则的列表
        ModConfig.getInstance().setRegexFilters(List.of("valid.*", "[invalid"));
        ModConfig.save();
        
        // 验证清理后的列表
        assertThat(ModConfig.getInstance().getRegexFilters())
            .containsExactly("valid.*");
    }

    @Test
    void load_shouldHandleInvalidJson() throws IOException {
        Files.writeString(tempConfig, "{ invalid json }");
        ModConfig.load();
        assertThat(ModConfig.getInstance().enabled).isTrue();
    }

    @Test
    void shouldCompileValidPatterns() {
        // 设置有效正则列表
        ModConfig.getInstance().setRegexFilters(List.of("valid.*", "[a-z]+"));
        ModConfig.getInstance().updateCompiledPatterns();
        
        List<Pattern> compiled = ModConfig.getInstance().getCompiledPatterns();
        // 验证编译数量
        assertThat(compiled).hasSize(2);
        // 验证模式内容
        assertThat(compiled.get(0).pattern()).isEqualTo("valid.*");
    }

    @Test
    void shouldSkipInvalidPatternsDuringCompilation() {
        // 设置混合正则列表
        ModConfig.getInstance().setRegexFilters(List.of("valid.*", "[invalid["));
        ModConfig.getInstance().updateCompiledPatterns();
        
        List<Pattern> compiled = ModConfig.getInstance().getCompiledPatterns();
        // 验证跳过无效正则
        assertThat(compiled).hasSize(1);
        // 验证保留的有效正则
        assertThat(compiled.get(0).pattern()).isEqualTo("valid.*");
    }

    // 空正则列表处理
    @Test
    void shouldHandleEmptyRegexList() {
        // 设置空列表
        ModConfig.getInstance().setRegexFilters(List.of());
        ModConfig.save();
        ModConfig.load();
        // 验证编译结果为空
        assertThat(ModConfig.getInstance().getCompiledPatterns()).isEmpty();
    }

    // 大量正则表达式处理
    @Test
    void shouldHandleLargeNumberOfPatterns() {
        // 准备测试数据 (50 个有效 + 50 个无效)
        List<String> patterns = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            patterns.add("valid" + i + ".*");
            patterns.add("[invalid" + i + "["); // 无效模式
        }
        
        ModConfig.getInstance().setRegexFilters(patterns);
        ModConfig.save();
        ModConfig.load();
        
        // 验证只编译有效正则
        assertThat(ModConfig.getInstance().getCompiledPatterns())
            .hasSize(50);
    }
}

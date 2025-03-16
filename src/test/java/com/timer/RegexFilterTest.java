package com.timer;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.text.Text;
import org.junit.jupiter.api.*;

public class RegexFilterTest {
    private static Path tempConfig;
    private ModConfig config;

    @BeforeAll
    static void setupTempConfig() throws IOException {
        tempConfig = Files.createTempFile("regexfilter-test", ".json");
        ModConfig.setConfigPathForTest(tempConfig);
    }

    @AfterAll
    static void cleanupTempConfig() throws IOException {
        Files.deleteIfExists(tempConfig);
    }

    @BeforeEach
    void setup() {
        ModConfig.load(); // 确保配置在最新状态
        config = ModConfig.getInstance();
        config.enabled = true;
        config.regexFilters = List.of("^\\[System\\].*", ".*(cheat|hack).*", "(?i)specific phrase");
        ModConfig.save();
        ModConfig.load(); // 应用配置
    }

    @Test
    void shouldBlockMatchingMessages() {
        assertShouldBlock("[System] Server restart", true);
        assertShouldBlock("Using hack tool", true);
        assertShouldBlock("SPECIFIC PHRASE", true); // 测试不区分大小写
        assertShouldBlock("Specific Phrase", true); // 测试混合大小写
    }

    @Test
    void shouldAllowNonMatchingMessages() {
        assertShouldBlock("Normal message", false);
        assertShouldBlock("[Info] Player joined", false);
        assertShouldBlock("Specificphrase", false);
    }

    @AfterEach
    void reset() {
        // 重置配置到初始状态
        ModConfig.getInstance().enabled = true;
        ModConfig.getInstance().regexFilters = new ArrayList<>();
        ModConfig.save();
        ModConfig.load();
    }

    @Test
    void shouldHandleInvalidRegexSafely() {
        // 设置包含无效正则的配置
        ModConfig.getInstance().regexFilters = List.of("^valid.*", "[invalid[regex"); // 添加 ^ 严格匹配开头
        ModConfig.save();
        ModConfig.load();

        // 获取最新实例
        ModConfig config = ModConfig.getInstance();

        // 验证正则列表和匹配逻辑
        assertThat(config.getCompiledPatterns()).hasSize(1);
        Pattern validPattern = config.getCompiledPatterns().get(0);

        // 验证正则表达式
        assertThat(validPattern.pattern()).isEqualTo("^valid.*");

        // 确保正则表达式不包含意外标志
        assertThat(validPattern.flags() & Pattern.CASE_INSENSITIVE).isEqualTo(0);

        // 验证有效正则匹配正确字符串
        assertThat(validPattern.matcher("valid123").find()).isTrue();
        assertThat(validPattern.matcher("[invalid[regex").find()).isFalse();

        // 更新断言以反映正确行为
        assertShouldBlock("valid123", true);
        assertShouldBlock("[invalid[regex", false);
    }

    @Test
    void shouldRespectCaseInsensitiveFlag() {
        ModConfig.getInstance().regexFilters = List.of("(?i)casetest");
        ModConfig.save();
        ModConfig.load(); // 重新加载配置

        // 验证编译后的正则表达式标志
        List<Pattern> patterns = ModConfig.getInstance().getCompiledPatterns();
        assertThat(patterns).hasSize(1);
        Pattern pattern = patterns.get(0);
        assertThat(pattern.flags() & Pattern.CASE_INSENSITIVE).isNotEqualTo(0);

        // 测试不同大小写的消息
        assertShouldBlock("CASETEST", true);
        assertShouldBlock("casetest", true);
        assertShouldBlock("CaseTest", true);
        assertShouldBlock("CaSeTeSt", true);
    }

    private void assertShouldBlock(String message, boolean expected) {
        boolean actual = RegexFilterClient.shouldAllowMessage(Text.of(message));
        assertThat(actual).isEqualTo(!expected);
    }
}

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
        config.regexFilters = List.of(
            "^\\[System\\].*", 
            ".*(cheat|hack).*", 
            "(?i)specific phrase"
        );
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
        ModConfig.getInstance().regexFilters = List.of("valid.*", "[invalid[regex");
        ModConfig.save();
        ModConfig.load();

    // 获取最新实例
        ModConfig config = ModConfig.getInstance();

    // 验证正则列表和匹配逻辑
        assertThat(config.getCompiledPatterns()).hasSize(1);
        Pattern validPattern = config.getCompiledPatterns().get(0);
    
    // 添加诊断日志
        System.out.println("Testing pattern: " + validPattern.pattern());
        boolean matches = validPattern.matcher("[invalid[regex").find();
        System.out.println("Pattern matches '[invalid[regex': " + matches);
    
        assertThat(matches).isFalse(); // 明确断言不匹配
        assertShouldBlock("valid123", true);
        assertShouldBlock("[invalid[regex", false);
    }

    @Test
    void shouldRespectCaseInsensitiveFlag() {
        // 测试大小写不敏感特性
        config.regexFilters = List.of("(?i)caseTest");
        ModConfig.save();
        
        assertShouldBlock("CASETEST", true);
        assertShouldBlock("casetest", true);
        assertShouldBlock("CaseTest", true);
    }

    private void assertShouldBlock(String message, boolean expected) {
        boolean actual = RegexFilterClient.shouldAllowMessage(Text.of(message));
        assertThat(actual).isEqualTo(!expected);
    }
}

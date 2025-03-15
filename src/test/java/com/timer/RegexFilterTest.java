package com.timer;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
        config = ModConfig.getInstance();
        config.enabled = true;
        config.regexFilters = List.of(
            "^\\[System\\].*", 
            ".*(cheat|hack).*", 
            "(?i)specific phrase"
        );
        ModConfig.save();
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

    @Test
    void shouldHandleInvalidRegexSafely() {
    // 设置包含无效正则的配置
        config.regexFilters = List.of("valid.*", "[invalid[regex");
        ModConfig.save();
        ModConfig.load(); // 强制重新加载清理后的配置
    
    // 重新获取配置实例
        config = ModConfig.getInstance();
    
    // 验证清理后的正则列表
        assertThat(config.getCompiledPatterns()).hasSize(1);
        assertShouldBlock("valid123", true);
        assertShouldBlock("[invalid[regex", false); // 无效正则已被移除
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

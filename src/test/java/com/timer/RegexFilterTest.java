package com.timer;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import net.minecraft.text.Text;
import org.junit.jupiter.api.*;

public class RegexFilterTest {
    private ModConfig config;

    @BeforeEach
    void setup() {
        config = ModConfig.getInstance();
        config.enabled = true;
        config.regexFilters = List.of(
            "^\\[System\\].*", 
            ".*(cheat|hack).*", 
            "(?i)specific phrase" // 不区分大小写的正则
        );
        ModConfig.save(); // 触发预编译缓存更新
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

package com.timer;

import net.minecraft.text.Text;
import org.junit.jupiter.api.*;
import java.util.List;
import static com.google.common.truth.Truth.assertThat;

public class RegexFilterTest {
    private ModConfig config;
    
    @BeforeEach
    void setup() {
        config = ModConfig.getInstance();
        config.enabled = true;
        config.regexFilters = List.of(
            "^\\[System\\].*", 
            ".*(cheat|hack).*",
            "specific phrase"
        );
    }
    
    @Test
    void shouldBlockMatchingMessages() {
        assertShouldBlock("[System] Server restart", true);
        assertShouldBlock("Using hack tool", true);
        assertShouldBlock("SPECIFIC PHRASE", true); // 测试大小写敏感
    }
    
    @Test
    void shouldAllowNonMatchingMessages() {
        assertShouldBlock("Normal message", false);
        assertShouldBlock("[Info] Player joined", false);
    }
    
    @Test
    void shouldHandleInvalidRegexSafely() {
        config.regexFilters = List.of("valid.*", "[invalid[regex");
        assertShouldBlock("valid123", true);
        // 无效正则不应导致异常
        assertShouldBlock("any message", false); 
    }
    
    private void assertShouldBlock(String message, boolean expected) {
        boolean actual = RegexFilterClient.shouldAllowMessage(Text.of(message));
        assertThat(actual).isEqualTo(!expected);
    }
}
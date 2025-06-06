package com.timer;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.text.Text;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;

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
        ModConfig.load();
        config = ModConfig.getInstance();
        config.enabled = true;
        // 设置正则列表
        config.setRegexFilters(
                List.of("^\\[System\\].*", ".*(cheat|hack).*", "(?i)specific phrase"));
        ModConfig.save();
        ModConfig.load();
    }

    @ParameterizedTest(name = "消息 [{0}] 应被拦截: {1}")
    @MethodSource("messageFilteringProvider")
    void testMessageFiltering(String message, boolean expectedToBlock) {
        assertShouldBlock(message, expectedToBlock);
    }

    private static Stream<Arguments> messageFilteringProvider() {
        return Stream.of(
            Arguments.of("[System] Server restart", true),
            Arguments.of("Using hack tool", true),
            Arguments.of("SPECIFIC PHRASE", true),
            Arguments.of("Specific Phrase", true),
            Arguments.of("Normal message", false),
            Arguments.of("[Info] Player joined", false),
            Arguments.of("Specificphrase", false)
        );
    }

    @AfterEach
    void reset() {
        ModConfig.getInstance().enabled = true;
        // 重置正则列表
        ModConfig.getInstance().setRegexFilters(List.of());
        ModConfig.save();
        ModConfig.load();
    }

    @Test
    void shouldHandleInvalidRegexSafely() {
        // 准备包含有效和无效正则的列表
        List<String> mixedPatterns = List.of("^valid.*", "[invalid[regex");
        ModConfig.getInstance().setRegexFilters(mixedPatterns);
        ModConfig.save();
        ModConfig.load();

        ModConfig config = ModConfig.getInstance();
        // 验证只编译有效正则
        assertThat(config.getCompiledPatterns()).hasSize(1);
        Pattern validPattern = config.getCompiledPatterns().get(0);
        
        // 验证模式内容
        assertThat(validPattern.pattern()).isEqualTo("^valid.*");
        // 验证大小写敏感标志
        assertThat(validPattern.flags() & Pattern.CASE_INSENSITIVE).isEqualTo(0);
        
        // 验证过滤行为
        assertShouldBlock("valid123", true);
        assertShouldBlock("[invalid[regex", false);
    }

    @Test
    void shouldRespectCaseInsensitiveFlag() {
        ModConfig.getInstance().setRegexFilters(List.of("(?i)casetest"));
        ModConfig.save();
        ModConfig.load();

        List<Pattern> patterns = ModConfig.getInstance().getCompiledPatterns();
        assertThat(patterns).hasSize(1);
        Pattern pattern = patterns.get(0);
        assertThat(pattern.flags() & Pattern.CASE_INSENSITIVE).isNotEqualTo(0);
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

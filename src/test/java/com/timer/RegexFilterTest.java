package com.timer;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import net.minecraft.text.Text;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        config.setRegexFilters(List.of("^\\[System\\].*", ".*(cheat|hack).*", "(?i)specific phrase"));
        ModConfig.save();
        ModConfig.load();
    }

    @ParameterizedTest
    @CsvSource({
        "[System] Server restart, true",
        "Using hack tool, true",
        "SPECIFIC PHRASE, true",
        "Specific Phrase, true",
        "Normal message, false",
        "[Info] Player joined, false",
        "Specificphrase, false"
    })
    void testMessageFiltering(String message, boolean expectedToBlock) {
        assertShouldBlock(message, expectedToBlock);
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
        // 设置包含无效正则的配置
        ModConfig.getInstance().setRegexFilters(List.of("^valid.*", "[invalid[regex"));
        ModConfig.save();
        ModConfig.load();

        ModConfig config = ModConfig.getInstance();
        assertThat(config.getCompiledPatterns()).hasSize(1);
        Pattern validPattern = config.getCompiledPatterns().get(0);
        assertThat(validPattern.pattern()).isEqualTo("^valid.*");
        assertThat(validPattern.flags() & Pattern.CASE_INSENSITIVE).isEqualTo(0);
        assertThat(validPattern.matcher("valid123").find()).isTrue();
        assertThat(validPattern.matcher("[invalid[regex").find()).isFalse();
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

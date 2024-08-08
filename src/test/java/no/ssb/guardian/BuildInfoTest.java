package no.ssb.guardian;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildInfoTest {

    @Test
    void testBuildInfo() {
        assertThat(BuildInfo.INSTANCE.getVersion()).isNotEqualTo("unknown");
        assertThat(BuildInfo.INSTANCE.getBuildTimestamp()).isNotEqualTo("unknown");
    }
}
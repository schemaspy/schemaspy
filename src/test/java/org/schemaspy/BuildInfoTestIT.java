package org.schemaspy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

//Only works with maven since it adds the original-jar and dependencies to classpath
public class BuildInfoTestIT {

    private static final String UNKNOWN = "Unknown";

    private BuildInfo buildInfo = new BuildInfo();

    @Test
    public void getVersion() {
        assertThat(buildInfo.getVersion()).isNotEqualToIgnoringCase(UNKNOWN);
    }

    @Test
    public void getRevision() {
        assertThat(buildInfo.getRevision()).isNotEqualToIgnoringCase(UNKNOWN);
    }

    @Test
    public void getBranch() {
        assertThat(buildInfo.getBranch()).isNotEqualToIgnoringCase(UNKNOWN);
    }

    @Test
    public void getBuilder() {
        assertThat(buildInfo.getBuilder()).isNotEqualToIgnoringCase(UNKNOWN);
    }

    @Test
    public void getJDK() {
        assertThat(buildInfo.getJDK()).isNotEqualToIgnoringCase(UNKNOWN);
    }

    @Test
    public void getBuildTime() {
        assertThat(buildInfo.getBuildTime()).isNotEqualToIgnoringCase(UNKNOWN);
    }
}
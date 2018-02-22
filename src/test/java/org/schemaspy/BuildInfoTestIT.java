package org.schemaspy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

//Only works with maven since it adds the original-jar and dependencies to classpath
public class BuildInfoTestIT {

    private static final String UNKNOWN = "Unknown";
    private static final String NON_BUILD = "NonBuild";

    private BuildInfo buildInfo = new BuildInfo();

    @Test
    public void getVersion() {
        assertThat(buildInfo.getVersion()).isNotEqualToIgnoringCase(UNKNOWN);
        assertThat(buildInfo.getVersion()).isNotEqualToIgnoringCase(NON_BUILD);
    }

    @Test
    public void getRevision() {
        assertThat(buildInfo.getRevision()).isNotEqualToIgnoringCase(UNKNOWN);
        assertThat(buildInfo.getRevision()).isNotEqualToIgnoringCase(NON_BUILD);
    }

    @Test
    public void getBranch() {
        assertThat(buildInfo.getBranch()).isNotEqualToIgnoringCase(UNKNOWN);
        assertThat(buildInfo.getBranch()).isNotEqualToIgnoringCase(NON_BUILD);
    }

    @Test
    public void getBuilder() {
        assertThat(buildInfo.getBuilder()).isNotEqualToIgnoringCase(UNKNOWN);
        assertThat(buildInfo.getBuilder()).isNotEqualToIgnoringCase(NON_BUILD);
    }

    @Test
    public void getJDK() {
        assertThat(buildInfo.getJDK()).isNotEqualToIgnoringCase(UNKNOWN);
        assertThat(buildInfo.getJDK()).isNotEqualToIgnoringCase(NON_BUILD);
    }

    @Test
    public void getBuildTime() {
        assertThat(buildInfo.getBuildTime()).isNotEqualToIgnoringCase(UNKNOWN);
        assertThat(buildInfo.getBuildTime()).isNotEqualToIgnoringCase(NON_BUILD);
    }
}
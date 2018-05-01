package org.schemaspy;

import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

//This will fail in ide but work in maven, since maven uses the built jar on classpath.
public class RevisionTestIT {

    private Revision revision = new Revision();

    @Test
    public void isNotUnknown() throws IOException {
        assertThat(revision.toString()).isNotEqualToIgnoringCase("unknown");
        assertThat(revision.toString()).isNotEqualToIgnoringCase("NonBuild");
    }
}

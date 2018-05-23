package org.schemaspy;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.schemaspy.testing.ExitCodeRule;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
public class MainTest {

    @Rule
    public ExitCodeRule exitCodeRule = new ExitCodeRule();

    private AnnotationConfigApplicationContext annotationConfigApplicationContext =
            new AnnotationConfigApplicationContext();

    private SchemaAnalyzer schemaAnalyzer = mock(SchemaAnalyzer.class);

    @Test
    public void callsSystemExit() {
        try {
            Main.main(
                    "-t", "mysql",
                    "-sso",
                    "-o", "target/tmp",
                    "-host", "localhost",
                    "-port", "123154",
                    "-db", "qwerty"
            );
        } catch (SecurityException ignore) { }
        assertThat(exitCodeRule.getExitCode()).isEqualTo(3);
    }

}

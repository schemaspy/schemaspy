package org.schemaspy.output.html.mustache.diagrams;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.schemaspy.model.Database;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.DiagramResult;
import org.schemaspy.output.diagram.RenderException;
import org.schemaspy.output.diagram.SummaryDiagram;
import org.schemaspy.output.dot.schemaspy.DotFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MustacheSummaryDiagramFactoryTest {

    private static final String FILE_PREFIX = "relationships";

    @TempDir
    Path temporaryFolder;

    private final ProgressListener progressListener = mock(ProgressListener.class);

    @Test
    void noDiagrams() throws IOException {
        DotFormatter dotProducer = mock(DotFormatter.class);
        SummaryDiagram mustacheDiagramFactory = mock(SummaryDiagram.class);
        when(mustacheDiagramFactory.generateSummaryDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));
        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory =
            new MustacheSummaryDiagramFactory(
                dotProducer,
                mustacheDiagramFactory,
                false,
                false,
                temporaryFolder.resolve("noDiagrams").toFile(),
                progressListener
            );

        Database database = mock(Database.class);
        List<Table> noTables = Collections.emptyList();

        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, noTables);
        assertThat(results.getDiagrams()).isEmpty();
    }

    @Test
    void realDiagrams() throws IOException {
        DotFormatter dotProducer = mock(DotFormatter.class);

        SummaryDiagram mustacheDiagramFactory = mock(SummaryDiagram.class);
        when(mustacheDiagramFactory.generateSummaryDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));
        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory =
            new MustacheSummaryDiagramFactory(
                dotProducer,
                mustacheDiagramFactory,
                true,
                false,
                temporaryFolder.resolve("noDiagrams").toFile(),
                progressListener
            );

        Database database = mock(Database.class);
        when(database.getRemoteTables()).thenReturn(Collections.emptyList());
        Table tableReal = mock(Table.class);
        when(tableReal.isOrphan(false)).thenReturn(false);
        List<Table> tables = Collections.singletonList(tableReal);

        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, tables);
        assertThat(results.getDiagrams()).hasSize(2);
        assertThat(results.getDiagrams().get(0).getActive()).isNotEmpty();
        assertThat(results.getDiagrams().get(1).getActive()).isNullOrEmpty();
    }

    @Test
    void realAndImpliedDiagrams() throws IOException {
        DotFormatter dotProducer = mock(DotFormatter.class);

        SummaryDiagram mustacheDiagramFactory = mock(SummaryDiagram.class);
        when(mustacheDiagramFactory.generateSummaryDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));

        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory =
            new MustacheSummaryDiagramFactory(
                dotProducer,
                mustacheDiagramFactory,
                true,
                true,
                temporaryFolder.resolve("noDiagrams").toFile(),
                progressListener
            );

        Database database = mock(Database.class);
        when(database.getRemoteTables()).thenReturn(Collections.emptyList());
        Table tableReal = mock(Table.class);
        when(tableReal.isOrphan(false)).thenReturn(false);
        List<Table> tables = Collections.singletonList(tableReal);

        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, tables);
        assertThat(results.getDiagrams()).hasSize(4);
        assertThat(results.getDiagrams().get(0).getActive()).isNotEmpty();
        assertThat(results.getDiagrams().get(1).getActive()).isNullOrEmpty();
        assertThat(results.getDiagrams().get(2).getActive()).isNullOrEmpty();
        assertThat(results.getDiagrams().get(3).getActive()).isNullOrEmpty();
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void exceptionsAreCaught() throws IOException {
        SummaryDiagram mustacheDiagramFactory = mock(SummaryDiagram.class);
        doThrow(new RenderException("byDesign")).
                when(mustacheDiagramFactory)
                .generateSummaryDiagram(
                        any(File.class),
                        anyString());


        File outputDir = temporaryFolder.resolve("noDiagrams").toFile();

        Path summaryPath = outputDir.toPath().resolve("diagrams").resolve("summary");
        Files.createDirectories(summaryPath);
        Files.createFile(summaryPath.resolve(FILE_PREFIX + ".real.compact.dot"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--")));
        Files.createFile(summaryPath.resolve(FILE_PREFIX + ".implied.compact.dot"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--")));
        Files.createFile(summaryPath.resolve(FILE_PREFIX + ".implied.large.dot"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--")));

        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory =
            new MustacheSummaryDiagramFactory(
                mock(DotFormatter.class),
                mustacheDiagramFactory,
                true,
                true,
                outputDir,
                progressListener
            );

        Database database = mock(Database.class);
        when(database.getRemoteTables()).thenReturn(Collections.emptyList());
        Table tableReal = mock(Table.class);
        when(tableReal.isOrphan(false)).thenReturn(false);
        List<Table> tables = Collections.singletonList(tableReal);

        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, tables);
        assertThat(results.getDiagrams()).isEmpty();
        assertThat(results.getOutputExceptions()).hasSize(4);
    }
}
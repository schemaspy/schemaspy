package org.schemaspy.output.html.mustache.diagrams;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.schemaspy.analyzer.ImpliedConstraintsFinder;
import org.schemaspy.model.Database;
import org.schemaspy.model.ImpliedForeignKeyConstraint;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.RenderException;
import org.schemaspy.output.diagram.DiagramResult;
import org.schemaspy.output.diagram.SummaryDiagram;
import org.schemaspy.output.dot.schemaspy.DotFormatter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class MustacheSummaryDiagramFactoryTest {

    private static final String FILE_PREFIX = "relationships";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void noDiagrams() throws IOException {
        DotFormatter dotProducer = mock(DotFormatter.class);
        SummaryDiagram mustacheDiagramFactory = mock(SummaryDiagram.class);
        when(mustacheDiagramFactory.generateSummaryDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));
        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(dotProducer, mustacheDiagramFactory,false,false, temporaryFolder.newFolder("noDiagrams"));

        Database database = mock(Database.class);
        List<Table> noTables = Collections.emptyList();
        ProgressListener progressListener = mock(ProgressListener.class);


        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, noTables, true, progressListener);
        assertThat(results.getDiagrams()).isEmpty();
    }

    @Test
    public void realDiagrams() throws IOException {
        DotFormatter dotProducer = mock(DotFormatter.class);

        SummaryDiagram mustacheDiagramFactory = mock(SummaryDiagram.class);
        when(mustacheDiagramFactory.generateSummaryDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));
        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(dotProducer, mustacheDiagramFactory, true,false, temporaryFolder.newFolder("noDiagrams"));

        Database database = mock(Database.class);
        when(database.getRemoteTables()).thenReturn(Collections.emptyList());
        Table tableReal = mock(Table.class);
        when(tableReal.isOrphan(false)).thenReturn(false);
        List<Table> tables = Collections.singletonList(tableReal);
        ProgressListener progressListener = mock(ProgressListener.class);


        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, tables, true, progressListener);
        assertThat(results.getDiagrams()).hasSize(2);
        assertThat(results.getDiagrams().get(0).getActive()).isNotEmpty();
        assertThat(results.getDiagrams().get(1).getActive()).isNullOrEmpty();
    }

    @Test
    public void realAndImpliedDiagrams() throws IOException {
        DotFormatter dotProducer = mock(DotFormatter.class);

        SummaryDiagram mustacheDiagramFactory = mock(SummaryDiagram.class);
        when(mustacheDiagramFactory.generateSummaryDiagram(any(File.class),anyString())).then(invocation -> mock(DiagramResult.class));

        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(dotProducer, mustacheDiagramFactory, true,true, temporaryFolder.newFolder("noDiagrams"));

        Database database = mock(Database.class);
        when(database.getRemoteTables()).thenReturn(Collections.emptyList());
        Table tableReal = mock(Table.class);
        when(tableReal.isOrphan(false)).thenReturn(false);
        List<Table> tables = Collections.singletonList(tableReal);
        ProgressListener progressListener = mock(ProgressListener.class);


        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, tables, true, progressListener);
        assertThat(results.getDiagrams()).hasSize(4);
        assertThat(results.getDiagrams().get(0).getActive()).isNotEmpty();
        assertThat(results.getDiagrams().get(1).getActive()).isNullOrEmpty();
        assertThat(results.getDiagrams().get(2).getActive()).isNullOrEmpty();
        assertThat(results.getDiagrams().get(3).getActive()).isNullOrEmpty();
    }

    @Test
    public void exceptionsAreCaught() throws IOException {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
        SummaryDiagram mustacheDiagramFactory = mock(SummaryDiagram.class);
        doThrow(new RenderException("byDesign")).
                when(mustacheDiagramFactory)
                .generateSummaryDiagram(
                        any(File.class),
                        anyString());


        File outputDir = temporaryFolder.newFolder("noDiagrams");

        Path summaryPath = outputDir.toPath().resolve("diagrams").resolve("summary");
        Files.createDirectories(summaryPath);
        Files.createFile(summaryPath.resolve(FILE_PREFIX + ".real.compact.dot"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--")));
        Files.createFile(summaryPath.resolve(FILE_PREFIX + ".implied.compact.dot"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--")));
        Files.createFile(summaryPath.resolve(FILE_PREFIX + ".implied.large.dot"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--")));

        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(mock(DotFormatter.class),  mustacheDiagramFactory, true,true, outputDir);

        Database database = mock(Database.class);
        when(database.getRemoteTables()).thenReturn(Collections.emptyList());
        Table tableReal = mock(Table.class);
        when(tableReal.isOrphan(false)).thenReturn(false);
        List<Table> tables = Collections.singletonList(tableReal);
        ProgressListener progressListener = mock(ProgressListener.class);


        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, tables, true, progressListener);
        assertThat(results.getDiagrams()).isEmpty();
        assertThat(results.getOutputExceptions()).hasSize(4);
    }
}
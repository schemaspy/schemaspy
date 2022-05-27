package org.schemaspy.output.html.mustache.diagrams;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.schemaspy.analyzer.ImpliedConstraintsFinder;
import org.schemaspy.model.Database;
import org.schemaspy.model.ImpliedForeignKeyConstraint;
import org.schemaspy.model.ProgressListener;
import org.schemaspy.model.Table;
import org.schemaspy.output.diagram.DiagramException;
import org.schemaspy.output.dot.schemaspy.DotFormatter;
import org.schemaspy.view.MustacheTableDiagram;
import org.schemaspy.view.WriteStats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MustacheSummaryDiagramFactoryTest {

    private static final String FILE_PREFIX = "relationships";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void noDiagrams() throws IOException {
        DotFormatter dotProducer = mock(DotFormatter.class);
        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);
        when(mustacheDiagramFactory.generateSummaryDiagram(anyString(),any(File.class),anyString())).then(invocation -> new MustacheTableDiagram());
        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(dotProducer, mustacheDiagramFactory,null, temporaryFolder.newFolder("noDiagrams"));

        Database database = mock(Database.class);
        List<Table> noTables = Collections.emptyList();
        ProgressListener progressListener = mock(ProgressListener.class);


        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, noTables, false, true, progressListener);
        assertThat(results.getDiagrams()).isEmpty();
        assertThat(results.hasRealRelationships()).isFalse();
        assertThat(results.getImpliedConstraints()).isEmpty();
        assertThat(results.getStats().getNumTablesWritten()).isEqualTo(0);
        assertThat(results.getStats().getNumViewsWritten()).isEqualTo(0);
    }

    @Test
    public void realDiagrams() throws IOException {
        DotFormatter dotProducer = mock(DotFormatter.class);

        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);
        when(mustacheDiagramFactory.generateSummaryDiagram(anyString(),any(File.class),anyString())).then(invocation -> new MustacheTableDiagram());
        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(dotProducer, mustacheDiagramFactory,null, temporaryFolder.newFolder("noDiagrams"));

        Database database = mock(Database.class);
        when(database.getRemoteTables()).thenReturn(Collections.emptyList());
        Table tableReal = mock(Table.class);
        when(tableReal.isOrphan(false)).thenReturn(false);
        List<Table> tables = Collections.singletonList(tableReal);
        ProgressListener progressListener = mock(ProgressListener.class);


        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, tables, false, true, progressListener);
        assertThat(results.getDiagrams().size()).isEqualTo(2);
        assertThat(results.getDiagrams().get(0).getActive()).isNotEmpty();
        assertThat(results.getDiagrams().get(1).getActive()).isNullOrEmpty();
        assertThat(results.hasRealRelationships()).isTrue();
        assertThat(results.getImpliedConstraints()).isEmpty();
    }

    @Test
    public void realAndImpliedDiagrams() throws IOException {
        DotFormatter dotProducer = mock(DotFormatter.class);

        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);
        when(mustacheDiagramFactory.generateSummaryDiagram(anyString(),any(File.class),anyString())).then(invocation -> new MustacheTableDiagram());

        ImpliedConstraintsFinder impliedConstraintsFinder = mock(ImpliedConstraintsFinder.class);
        when(impliedConstraintsFinder.find(any(Collection.class))).then(invocation -> {
            ImpliedForeignKeyConstraint impliedForeignKeyConstraint = mock(ImpliedForeignKeyConstraint.class);
            return Collections.singletonList(impliedForeignKeyConstraint);
        });

        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(dotProducer, mustacheDiagramFactory, impliedConstraintsFinder, temporaryFolder.newFolder("noDiagrams"));

        Database database = mock(Database.class);
        when(database.getRemoteTables()).thenReturn(Collections.emptyList());
        Table tableReal = mock(Table.class);
        when(tableReal.isOrphan(false)).thenReturn(false);
        List<Table> tables = Collections.singletonList(tableReal);
        ProgressListener progressListener = mock(ProgressListener.class);


        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, tables, true, true, progressListener);
        assertThat(results.getDiagrams().size()).isEqualTo(4);
        assertThat(results.getDiagrams().get(0).getActive()).isNotEmpty();
        assertThat(results.getDiagrams().get(1).getActive()).isNullOrEmpty();
        assertThat(results.getDiagrams().get(2).getActive()).isNullOrEmpty();
        assertThat(results.getDiagrams().get(3).getActive()).isNullOrEmpty();
        assertThat(results.hasRealRelationships()).isTrue();
        assertThat(results.getImpliedConstraints().size()).isEqualTo(1);
    }

    @Test
    public void exceptionsAreCaught() throws IOException {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
        MustacheDiagramFactory mustacheDiagramFactory = mock(MustacheDiagramFactory.class);
        doThrow(new DiagramException("byDesign")).
                when(mustacheDiagramFactory)
                .generateSummaryDiagram(
                        anyString(),
                        any(File.class),
                        anyString());

        ImpliedConstraintsFinder impliedConstraintsFinder = mock(ImpliedConstraintsFinder.class);
        when(impliedConstraintsFinder.find(any(Collection.class))).then(invocation -> {
            ImpliedForeignKeyConstraint impliedForeignKeyConstraint = mock(ImpliedForeignKeyConstraint.class);
            return Collections.singletonList(impliedForeignKeyConstraint);
        });
        File outputDir = temporaryFolder.newFolder("noDiagrams");

        Path summaryPath = outputDir.toPath().resolve("diagrams").resolve("summary");
        Files.createDirectories(summaryPath);
        Files.createFile(summaryPath.resolve(FILE_PREFIX + ".real.compact.dot"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--")));
        Files.createFile(summaryPath.resolve(FILE_PREFIX + ".implied.compact.dot"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--")));
        Files.createFile(summaryPath.resolve(FILE_PREFIX + ".implied.large.dot"), PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("r--r--r--")));

        MustacheSummaryDiagramFactory mustacheSummaryDiagramFactory = new MustacheSummaryDiagramFactory(mock(DotFormatter.class), mustacheDiagramFactory, impliedConstraintsFinder, outputDir);

        Database database = mock(Database.class);
        when(database.getRemoteTables()).thenReturn(Collections.emptyList());
        Table tableReal = mock(Table.class);
        when(tableReal.isOrphan(false)).thenReturn(false);
        List<Table> tables = Collections.singletonList(tableReal);
        ProgressListener progressListener = mock(ProgressListener.class);


        MustacheSummaryDiagramResults results = mustacheSummaryDiagramFactory.generateSummaryDiagrams(database, tables, true, true, progressListener);
        assertThat(results.getDiagrams()).isEmpty();
        assertThat(results.getOutputExceptions().size()).isEqualTo(4);
    }
}
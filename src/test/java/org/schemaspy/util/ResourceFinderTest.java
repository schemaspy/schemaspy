/*
 * Copyright (C) 2017 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.util;

import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
public class ResourceFinderTest {

    private ResourceFinder resourceFinder = new ResourceFinder();

    private byte[] data = "This is a test".getBytes(StandardCharsets.UTF_8);
    private String subPath = "resourceFinderTest";

    private String workDirStr = System.getProperty("user.dir");
    private Path workDir = Paths.get(workDirStr);
    private String homeDirStr = System.getProperty("user.home");
    private Path homeDir = Paths.get(homeDirStr);

    @Test
    public void canFindInWorkDirRoot() throws IOException {
        Path p = null;
        try {
            p = Files.createTempFile(workDir, "resourceFinderWorkDir", ".test");
            Files.write(p, data);
            assertThat(p.toAbsolutePath().toString()).startsWith(workDirStr);
            InputStream inputStream = resourceFinder.find(null, p.getFileName().toString());
            assertThat(inputStream).hasSameContentAs(new ByteArrayInputStream(data));
        } finally {
            if (Objects.nonNull(p)) {
                Files.delete(p);
            }
        }
    }

    @Test
    public void canFindInWorkDirRootSubPath() throws IOException {
        Path tmpFile = null;
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory(workDir, subPath);
            tmpFile = Files.createTempFile(tmpDir, "resourceFinderWorkDir", ".test");
            Files.write(tmpFile, data);
            assertThat(tmpFile.toAbsolutePath().toString()).startsWith(workDirStr + File.separator + subPath);
            InputStream inputStream = resourceFinder.find(tmpDir.getFileName().toString(), tmpFile.getFileName().toString());
            assertThat(inputStream).hasSameContentAs(new ByteArrayInputStream(data));
        } finally {
            if (Objects.nonNull(tmpFile)) {
                Files.delete(tmpFile);
                Files.delete(tmpDir);
            }
        }
    }

    @Test
    public void canFindInUserHomeDirRoot() throws IOException {
        Path p = null;
        try {
            p = Files.createTempFile(homeDir, "resourceFinderHomeDir", ".test");
            Files.write(p, data);
            assertThat(p.toAbsolutePath().toString()).startsWith(homeDirStr);
            InputStream inputStream = resourceFinder.find(null, p.getFileName().toString());
            assertThat(inputStream).hasSameContentAs(new ByteArrayInputStream(data));
        } finally {
            if (Objects.nonNull(p)) {
                Files.delete(p);
            }
        }
    }

    @Test
    public void canFindInUserHomeDirRootSubPath() throws IOException {
        Path tmpFile = null;
        Path tmpDir = null;
        try {
            tmpDir = Files.createTempDirectory(homeDir, subPath);
            tmpFile = Files.createTempFile(tmpDir, "resourceFinderHomeDir", ".test");
            Files.write(tmpFile, data);
            assertThat(tmpFile.toAbsolutePath().toString()).startsWith(homeDirStr + File.separator + subPath);
            InputStream inputStream = resourceFinder.find(tmpDir.getFileName().toString(), tmpFile.getFileName().toString());
            assertThat(inputStream).hasSameContentAs(new ByteArrayInputStream(data));
        } finally {
            if (Objects.nonNull(tmpFile)) {
                Files.delete(tmpFile);
                Files.delete(tmpDir);
            }
        }
    }

    @Test
    public void canFindInClasspathRoot() throws FileNotFoundException {
        InputStream inputStream = resourceFinder.find("", "resourceFinder.test");
        assertThat(inputStream).hasSameContentAs(new ByteArrayInputStream(data));
    }

    @Test
    public void canFindInClasspathSubPath() throws FileNotFoundException {
        InputStream inputStream = resourceFinder.find(subPath, "resourceFinder.test");
        assertThat(inputStream).hasSameContentAs(new ByteArrayInputStream(data));
    }

}
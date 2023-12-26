/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.output.diagram.graphviz;

import com.beust.jcommander.JCommander;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.schemaspy.testing.logback.Logback;
import org.schemaspy.testing.logback.LogbackExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
class GraphvizDotIT {

    @RegisterExtension
    static LogbackExtension logback = new LogbackExtension();

    @Test
    @EnabledOnOs(OS.LINUX)
    void version2_26_0() {
        GraphvizConfig graphvizConfig = parse("-gv", "src/test/resources/dotFakes/2.26.0");
        GraphvizDot graphvizDot = new GraphvizDot(graphvizConfig);
        assertThat(graphvizDot.isValid()).isTrue();
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void version2_31_0() {
        GraphvizConfig graphvizConfig = parse("-gv", "src/test/resources/dotFakes/2.31.0");
        GraphvizDot graphvizDot = new GraphvizDot(graphvizConfig);
        assertThat(graphvizDot.isValid()).isFalse();
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void version2_32_0() {
        GraphvizConfig graphvizConfig = parse("-gv", "src/test/resources/dotFakes/2.32.0");
        GraphvizDot graphvizDot = new GraphvizDot(graphvizConfig);
        assertThat(graphvizDot.isValid()).isTrue();
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    @Logback(GraphvizDot.class)
    void specifyRenderer() {
        logback.expect(Matchers.containsString("gd"));
        GraphvizConfig graphvizConfig = parse("-gv", "src/test/resources/dotFakes/2.32.0", "-renderer", ":gd");
        GraphvizDot graphvizDot = new GraphvizDot(graphvizConfig);
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    @Logback(GraphvizDot.class)
    void defaultRenderer() {
        logback.expect(Matchers.containsString("cairo"));
        GraphvizConfig graphvizConfig = parse("-gv", "src/test/resources/dotFakes/2.32.0");
        GraphvizDot graphvizDot = new GraphvizDot(graphvizConfig);
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    @Logback(GraphvizDot.class)
    void lowQualityRenderer() {
        logback.expect(Matchers.containsString("gd"));
        GraphvizConfig graphvizConfig = parse("-gv", "src/test/resources/dotFakes/2.32.0", "-lq");
        GraphvizDot graphvizDot = new GraphvizDot(graphvizConfig);
    }

    private GraphvizConfig parse(String... args) {
        GraphvizConfigCli graphvizConfigCli = new GraphvizConfigCli();
        JCommander jCommander = JCommander.newBuilder().build();
        jCommander.addObject(graphvizConfigCli);
        jCommander.parse(args);
        return graphvizConfigCli;
    }

}
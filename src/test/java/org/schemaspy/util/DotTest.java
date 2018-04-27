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
import org.schemaspy.Config;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assume.assumeThat;

/**
 * @author Nils Petzaell
 */
public class DotTest {

    @Test
    public void version2_26_0() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assumeThat(System.getProperty("os.name"), is("Linux"));
        Config config = Config.getInstance();
        config.setGraphvizDir(Paths.get("src/test/resources/dotFakes/2.26.0").toAbsolutePath().toFile());
        Dot dot = createDot();
        assertThat(dot.isValid()).isTrue();
    }

    @Test
    public void version2_28_0() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assumeThat(System.getProperty("os.name"), is("Linux"));
        Config config = Config.getInstance();
        config.setGraphvizDir(Paths.get("src/test/resources/dotFakes/2.28.0").toAbsolutePath().toFile());
        Dot dot = createDot();
        assertThat(dot.isValid()).isFalse();
    }

    @Test
    public void version2_32_0() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        assumeThat(System.getProperty("os.name"), is("Linux"));
        Config config = Config.getInstance();
        config.setGraphvizDir(Paths.get("src/test/resources/dotFakes/2.32.0").toAbsolutePath().toFile());
        Dot dot = createDot();
        assertThat(dot.isValid()).isTrue();
    }

    private Dot createDot() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        assumeThat(System.getProperty("os.name"), is("Linux"));
        Constructor<Dot> ctor = Dot.class.getDeclaredConstructor(new Class[0]);
        ctor.setAccessible(true);
        Dot dot = ctor.newInstance();
        return dot;
    }

}
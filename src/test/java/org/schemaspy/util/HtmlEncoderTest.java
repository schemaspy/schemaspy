/*
 * Copyright (c) 2018 Nils Petzaell
 *
 * This file is part of SchemaSpy.
 *
 *  SchemaSpy is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SchemaSpy is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.schemaspy.util;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Nils Petzaell
 */
@RunWith(JUnitParamsRunner.class)
public class HtmlEncoderTest {

    @Test
    @Parameters({
            "<, &lt;",
            ">, &gt;",
            "a, a"
    })
    public void encodeChar(char source, String target) {
        String encoded = HtmlEncoder.encodeToken(source);
        assertThat(encoded).isEqualTo(target);
    }

    @Test
    public void encodeStringNewLine(){
        String encoded = HtmlEncoder.encodeToken("\n");
        assertThat(encoded).isEqualTo("<br>" + System.lineSeparator());
    }

    @Test
    public void encodeCarrigeReturn(){
        String encoded = HtmlEncoder.encodeToken("\r");
        assertThat(encoded).isEqualTo("");
    }

}
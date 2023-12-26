/*
 * Copyright (C) 2017 Daniel Watt
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

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Daniel Watt
 */
class CaseInsensitiveMapTest {

    private CaseInsensitiveMap<String> map = new CaseInsensitiveMap<>();

    @Test
    void putGetContainsKey() {
        assertThat(map).isEmpty();

        map.put("key", "value");
        assertThat(map).hasSize(1);
        assertThat(map.get("key")).isEqualTo("value");
        assertThat(map.get("KEY")).isEqualTo("value");
        assertThat(map).containsKey("key");
        assertThat(map).containsKey("KEY");

        map.put("KEY", "value2");
        assertThat(map).hasSize(1);
        assertThat(map.get("key")).isEqualTo("value2");
        assertThat(map.get("KEY")).isEqualTo("value2");
        assertThat(map).containsKey("key");
        assertThat(map).containsKey("KEY");
    }

    @Test
    void putAll() {
        map.putAll(Collections.singletonMap("key","value"));
        assertThat(map.get("key")).isEqualTo("value");
        assertThat(map.get("KEY")).isEqualTo("value");

        map.putAll(Collections.singletonMap("KEY","value2"));
        assertThat(map.get("key")).isEqualTo("value2");
        assertThat(map.get("KEY")).isEqualTo("value2");
    }

    @Test
    void remove() {
        map = new CaseInsensitiveMap<>(1);
        assertThat(map).isEmpty();
        assertThat(map.remove("key")).isNull();
        assertThat(map).isEmpty();

        map.put("key","value");
        assertThat(map.remove("KEY")).isEqualTo("value");
        assertThat(map).isEmpty();

        map.put("key","value");
        assertThat(map.remove("key")).isEqualTo("value");
        assertThat(map).isEmpty();
    }

    @Test
    void nullPutIsNotSupported() {
        assertThatThrownBy(() -> map.put(null, "value"))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullRemoveNotSupported() {
        assertThatThrownBy(() -> map.remove(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullContainsKeyNotSupported() {
        assertThatThrownBy(() -> map.containsKey(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nullGetIsSupported() {
        assertThat(map.get(null)).isNull();
    }
}
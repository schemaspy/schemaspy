package org.schemaspy.util;

import org.junit.Before;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseInsensitiveMapTest {

    private CaseInsensitiveMap<String> map;

    @Before
    public void setup() {
        this.map = new CaseInsensitiveMap<>();
    }

    @Test
    public void putGetContainsKey() {
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
    public void putAll() {
        map.putAll(Collections.singletonMap("key","value"));
        assertThat(map.get("key")).isEqualTo("value");
        assertThat(map.get("KEY")).isEqualTo("value");

        map.putAll(Collections.singletonMap("KEY","value2"));
        assertThat(map.get("key")).isEqualTo("value2");
        assertThat(map.get("KEY")).isEqualTo("value2");
    }

    @Test
    public void remove() {
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

    @Test(expected = NullPointerException.class)
    public void nullPutIsNotSupported() {
        map.put(null, "value");
    }

    @Test(expected = NullPointerException.class)
    public void nullRemoveNotSupported() {
        map.remove(null);
    }

    @Test(expected = NullPointerException.class)
    public void nullContainsKeyNotSupported() {
        map.containsKey(null);
    }

    @Test
    public void nullGetIsSupported() {
        assertThat(map.get(null)).isNull();
    }


}
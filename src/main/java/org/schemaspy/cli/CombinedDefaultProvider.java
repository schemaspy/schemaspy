package org.schemaspy.cli;

import com.beust.jcommander.IDefaultProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CombinedDefaultProvider implements IDefaultProvider {

    private final List<String> booleans = Arrays.asList("schemaspy.sso", "schemaspy.debug", "schemaspy.nohtml", "schemaspy.vizjs", "schemaspy.all");

    private final IDefaultProvider[] iDefaultProviders;

    public CombinedDefaultProvider(IDefaultProvider...iDefaultProviders) {
        this.iDefaultProviders = iDefaultProviders;
    }

    @Override
    public String getDefaultValueFor(String optionName) {
        String defaultValue = getDefaultValue(optionName);
        if (booleans.contains(optionName)) {
            if (Objects.isNull(defaultValue)) {
                return Boolean.FALSE.toString();
            } else {
                return defaultValue.isEmpty() ? Boolean.TRUE.toString(): defaultValue;
            }
        }
        return defaultValue;
    }

    private String getDefaultValue(String optionName) {
        return Stream.of(iDefaultProviders)
                .map(iDefaultProvider -> iDefaultProvider.getDefaultValueFor(optionName))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }
}

package org.schemaspy.cli;

import com.beust.jcommander.IDefaultProvider;

public class EnvDefaultProvider implements IDefaultProvider {
    @Override
    public String getDefaultValueFor(String optionName) {
        return System.getenv(optionName);
    }
}

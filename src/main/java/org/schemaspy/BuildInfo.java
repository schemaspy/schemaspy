package org.schemaspy;

import org.schemaspy.util.JarFileFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;

@Component
public class BuildInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final JarFileFinder jarFileFinder = new JarFileFinder();

    private static final String UNKNOWN = "Unknown";

    private static final Name VERSION = new Name("Implementation-Version");
    private static final Name REVISION = new Name("Built-Revision");
    private static final Name BRANCH = new Name("Built-Branch");
    private static final Name BY = new Name("Built-By");
    private static final Name JDK = new Name("Build-Jdk");
    private static final Name BUILD_TIME = new Name("Built-At");

    private Attributes attributes;

    public BuildInfo() {
        try (JarFile jarFile = jarFileFinder.findJarFileForClass(this.getClass())){
            attributes = jarFile.getManifest().getMainAttributes();
        } catch (IOException e) {
            attributes = new Attributes();
            LOGGER.error("Failed to load attributes from manifest", e);
        }
    }

    public String getVersion() {
        return get(VERSION);
    }

    public String getRevision() {
        return get(REVISION);
    }

    public String getBranch() {
        return get(BRANCH);
    }

    public String getBuilder() {
        return get(BY);
    }

    public String getJDK() {
        return get(JDK);
    }

    public String getBuildTime() {
        return get(BUILD_TIME);
    }

    private String get(Name name) {
        return (String) attributes.getOrDefault(name, UNKNOWN);
    }

}

package org.schemaspy.input.db.driver;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Objects;
import java.util.Set;

public class Loader {

    private final LoaderConfig config;
    private Driver driver;
    private PathResolver pathResolver = new PathResolver();

    public Loader(LoaderConfig config) {
        this.config = config;
    }

    /**
     * Loads a driver using a custom classloader if supplied
     *
     * @return {@link java.sql.Driver}
     * @throws LoaderException
     */
    public Driver getDriver() throws LoaderException {
        if (Objects.nonNull(driver)) {
            return driver;
        }
        return loadDriver();
    }

    private Driver loadDriver() throws LoaderException {
        ClassLoader classLoader = createClassLoader();
        try {
            driver = (Driver) Class.forName(config.getDriverClass(), true, classLoader).newInstance();
            return driver;
        } catch (InstantiationException e) {
            throw new LoaderException("Unable to create Driver: " + config.getDriverClass(), e);
        } catch (IllegalAccessException e) {
            throw new LoaderException("Unable to access metadata for Driver: " + config.getDriverClass(), e);
        } catch (ClassNotFoundException e) {
            throw new LoaderException("Class not found for Driver: " + config.getDriverClass(), e);
        }
    }

    private ClassLoader createClassLoader() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        Set<URL> classPath = pathResolver.resolveDriverPath(config.getDriverPath());
        if (classPath.isEmpty()) {
            return classLoader;
        }
        return new URLClassLoader(classPath.toArray(new URL[classPath.size()]), classLoader);
    }
}

package org.schemaspy.util;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarFile;

public class JarFileFinder {

    public JarFile findJarFileForClass(Class cls) throws IOException {
        URL location = cls.getProtectionDomain().getCodeSource().getLocation();
        URLConnection urlConnection = location.openConnection();
        if (urlConnection instanceof JarURLConnection) {
            return ((JarURLConnection)urlConnection).getJarFile();
        } else {
            try {
                return new JarFile(location.getPath());
            } catch (IOException e) {
                throw new NotRunningFromJarException(location.getPath(), e);
            }
        }
    }
}

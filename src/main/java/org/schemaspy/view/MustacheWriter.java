package org.schemaspy.view;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by rkasa on 2016-03-22.
 */
public class MustacheWriter {
    private File outputDir;
    private HashMap<String, Object> scopes;
    private  String rootPath;
    private String databaseName;

    public MustacheWriter(File outputDir, HashMap<String, Object> scopes, String rootPath, String databaseName) {
        this.outputDir = outputDir;
        this.scopes = scopes;
        this.rootPath = rootPath;
        this.databaseName = databaseName;
    }
    public void write(String templatePath, String destination, String scriptFileName) {
        MustacheFactory mf = new DefaultMustacheFactory();
        MustacheFactory contentMf = new DefaultMustacheFactory();
        StringWriter content = new StringWriter();
        StringWriter result = new StringWriter();
        FileUtils fileUtils = new FileUtils();

        HashMap<String, Object> mainScope = new HashMap<String, Object>();
        URL containerTemplate = getClass().getResource("/layout/container.html");
        URL template = getClass().getResource(templatePath);

        try {
            Mustache mustache = mf.compile(template.getPath());
            mustache.execute(result, scopes).flush();

            mainScope.put("databaseName", databaseName);
            mainScope.put("content", result);
            mainScope.put("pageScript",scriptFileName);
            mainScope.put("rootPath", rootPath);

            Mustache mustacheContent = contentMf.compile(containerTemplate.getPath());
            mustacheContent.execute(content, mainScope).flush();

            fileUtils.writeStringToFile(new File(outputDir, destination), content.toString(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

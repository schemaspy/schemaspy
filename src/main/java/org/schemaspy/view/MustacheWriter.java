package org.schemaspy.view;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.schemaspy.Config;
import org.schemaspy.model.InvalidConfigurationException;

import java.io.*;
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
    private String templateDirectory = Config.getInstance().getTemplateDirectory();

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
        //URL containerTemplate = getClass().getResource(Paths.get(templateDirectory,"container.html").toString());
       // URL template = getClass().getResource(templatePath);

        try {
            Mustache mustache = mf.compile(getReader(new File(templateDirectory,templatePath).toString()),"template");
            mustache.execute(result, scopes).flush();

            mainScope.put("databaseName", databaseName);
            mainScope.put("content", result);
            mainScope.put("pageScript",scriptFileName);
            mainScope.put("rootPath", rootPath);

            Mustache mustacheContent = contentMf.compile(getReader(new File(templateDirectory,"container.html").toString()), "container");
            mustacheContent.execute(content, mainScope).flush();

            fileUtils.writeStringToFile(new File(outputDir, destination), content.toString(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static StringReader getReader(String fileName) throws IOException {
    	InputStream cssStream = null;
        if (new File(fileName).exists()){
        	cssStream = new FileInputStream(fileName);
        }else if (new File(System.getProperty("user.dir"), fileName).exists()){
	        	cssStream = new FileInputStream(fileName);
        }

        if (cssStream == null)
            throw new ParseException("Unable to find requested file: " + fileName);
        String inputStream = IOUtils.toString(cssStream, "UTF-8").toString();
        return new StringReader(inputStream);
    }

    /**
     * Indicates an exception in parsing the css
     */
    public static class ParseException extends InvalidConfigurationException {
        private static final long serialVersionUID = 1L;

        /**
         * @param cause root exception that caused the failure
         */
        public ParseException(Exception cause) {
            super(cause);
        }

        /**
         * @param msg textual description of the failure
         */
        public ParseException(String msg) {
            super(msg);
        }
    }

}

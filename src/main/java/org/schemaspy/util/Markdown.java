package org.schemaspy.util;

import org.pegdown.PegDownProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rkasa on 2016-04-11.
 */
public class Markdown {

    private final static PegDownProcessor processor = new PegDownProcessor();
    private final static HashMap<String, String> pages = new HashMap<>();

    public static String toHtml(String markdownText, String rootPath) {
        String text = markdownText;

        if (text != null) {
            text = addReferenceLink(text, rootPath);
            text = processor.markdownToHtml(text);
        }

        return text;
    }

    public static void registryPage(String name, String path) {
        pages.put(name, path);
    }

    public static String pagePath(String page) {
        return pages.get(page);
    }

    private static String addReferenceLink(String markdownText, String rootPath) {
        StringBuilder text = new StringBuilder(markdownText);
        String newLine = "\r\n";

        Pattern p = Pattern.compile("\\[(.*?)\\]");
        Matcher m = p.matcher(markdownText);

        List<String> links = new ArrayList<>();

        while(m.find()) {
            links.add(m.group(1));
        }

        if (!links.isEmpty()) {
            text.append(newLine).append(newLine);
        }

        for (String link : links) {
            String anchorLink = "";
            String pageLink = link;
            int anchorPosition = link.lastIndexOf('.');

            if (anchorPosition > -1) {
                anchorLink = link.substring(anchorPosition + 1).trim();
                pageLink = link.substring(0, anchorPosition);
            }

            String path = rootPath+pagePath(pageLink);
            if (!anchorLink.equals("")) {
				path = path + "#" + anchorLink;
			}
            text.append("[").append(link).append("]: ./").append(path).append(newLine);
        }

        return text.toString();
    }
}

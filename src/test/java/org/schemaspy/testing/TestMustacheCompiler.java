package org.schemaspy.testing;

import org.schemaspy.view.MustacheCompiler;
import org.schemaspy.view.PageData;

import java.io.IOException;
import java.io.Writer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestMustacheCompiler implements MustacheCompiler {

    private PageData pageData;
    private Writer writer;

    @Override
    public void write(PageData pageData, Writer writer) throws IOException {
        this.pageData = pageData;
        this.writer = writer;
    }

    public PageData pageData() {
        return pageData;
    }

    public Writer writer() {
        return writer;
    }

    @Override
    public String getRootPath(int depth) {
        return IntStream.range(0, depth).mapToObj(i -> "../").collect(Collectors.joining("", "", ""));
    }
}

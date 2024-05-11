package org.schemaspy.input.dbms.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

public final class CmFacade implements Comments {

    private final Node column;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public CmFacade(final Node column) {
        this.column = column;
    }
    
    @Override
    public String value() {
        NamedNodeMap attribs = this.column.getAttributes();
        String tmp;
        Node commentsNode = attribs.getNamedItem("comments");
        if (commentsNode == null) {
            commentsNode = attribs.getNamedItem("remarks");
            if (Objects.nonNull(commentsNode)) {
                LOGGER.warn("<remarks> has been deprecated");
            }
        }
        if (commentsNode != null) {
            tmp = commentsNode.getNodeValue().trim();
            return tmp.length() == 0 ? null : tmp;
        } else {
            return null;
        }
    }
}

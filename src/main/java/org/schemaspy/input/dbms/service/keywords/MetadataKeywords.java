package org.schemaspy.input.dbms.service.keywords;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import org.schemaspy.input.dbms.service.helper.UniformSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataKeywords implements Keywords {
    private final DatabaseMetaData metadata;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public MetadataKeywords(DatabaseMetaData metadata) {
        this.metadata = metadata;
    }

    @Override
    public Set<String> value() {
        String keywords = null;
        try {
            keywords = this.metadata.getSQLKeywords();
        } catch (SQLException e) {
            LOGGER.warn("Failed to fetch metadata", e);
        }
        return keywords != null
            ? new UniformSet(keywords.split(",")).value()
            : Collections.emptySet();
    }
}

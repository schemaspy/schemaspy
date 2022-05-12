/*
 * Copyright (C) 2019 AE Ibrahim
 *
 * This file is part of SchemaSpy.
 *
 * SchemaSpy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SchemaSpy. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.input.dbms.service;

import org.schemaspy.model.Database;
import org.schemaspy.model.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class SequenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SqlService sqlService;
    private final Properties dbProperties;

    public SequenceService(SqlService sqlService, Properties dbProperties) {
        this.sqlService = sqlService;
        this.dbProperties = dbProperties;
    }

    public void gatherSequences(Database database) {
        initSequences(database);
    }

    /**
     * Initializes sequences.
     *
     * @throws SQLException
     */
    private void initSequences(Database db) {
        String sql = dbProperties.getProperty("selectSequencesSql");

        if (sql != null) {

            try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String sequenceName = rs.getString("sequence_name");
                    Integer startValue = getOptionalInt(rs, "start_value", 1);
                    Integer increment = getOptionalInt(rs, "increment", 1);

                    Sequence sequence = new Sequence(sequenceName, startValue, increment);
                    db.getSequencesMap().put(sequenceName, sequence);
                }
            } catch (SQLException sqlException) {
                // don't die just because this failed
                LOGGER.warn("Failed to retrieve sequences using sql '{}'", sql, sqlException);
            }
        }
    }

    private static int getOptionalInt(ResultSet rs, String columnName, int defaultIfNotFound) {
        try {
            return rs.getInt(columnName);
        } catch (SQLException sqlException) {
            LOGGER.debug("Failed to get value for column '{}'", sqlException.getMessage(), sqlException);
            return defaultIfNotFound;
        }
    }
}

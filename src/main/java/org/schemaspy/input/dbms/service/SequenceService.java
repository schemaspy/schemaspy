/*
 * Copyright (C) 2019 Nils Petzaell
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

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.Routine;
import org.schemaspy.model.RoutineParameter;
import org.schemaspy.model.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SequenceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SqlService sqlService;

    public SequenceService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void gatherSequences(Config config, Database database) {
        initSequences(config, database);
    }

    /**
     * Initializes sequences.
     *
     * @throws SQLException
     */
    private void initSequences(Config config, Database db) {
        String sql = config.getDbProperties().getProperty("selectSequencesSql");

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
            LOGGER.debug("Failed to get value for column '{}'", sqlException);
            return defaultIfNotFound;
        }
    }
}

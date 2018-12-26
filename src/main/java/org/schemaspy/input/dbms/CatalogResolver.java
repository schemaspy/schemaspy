/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.input.dbms;

import org.schemaspy.model.InvalidConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Objects;

public class CatalogResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private DatabaseMetaData databaseMetaData;

    public CatalogResolver(DatabaseMetaData databaseMetaData) {
        this.databaseMetaData = databaseMetaData;
    }

    public String resolveCatalog(String currentCatalog) {
        if (Objects.isNull(currentCatalog)) {
            String catalog = null;
            try {
                catalog = databaseMetaData.getConnection().getCatalog();
                LOGGER.debug("Catalog not provided, queried jdbc driver and got '{}'", catalog);
            } catch (SQLException sqle) {
                LOGGER.error("Catalog (-cat) not provided, queried jdbc driver for catalog and failed", sqle);
            }

            if (Objects.isNull(catalog)) {
                throw new InvalidConfigurationException("Catalog (-cat) was not provided and unable to deduce catalog, wildcard catalog can be used -cat %");
            }

            return catalog;
        } else {
            return currentCatalog;
        }
    }
}

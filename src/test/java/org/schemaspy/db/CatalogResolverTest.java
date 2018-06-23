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
package org.schemaspy.db;

import org.junit.Test;
import org.schemaspy.model.InvalidConfigurationException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CatalogResolverTest {

    @Test
    public void catalogIsSameIfSet() {
        String expected = "providedCatalog";
        String provided = "providedCatalog";

        provided = new CatalogResolver(null).resolveCatalog(provided);
        assertThat(provided).isEqualTo(expected);
    }

    @Test
    public void catalogIsNullFetchFromJDBC() throws SQLException {
        String expected = "jdbcProvidedCatalog";
        Connection connection = mock(Connection.class);
        when(connection.getCatalog()).thenReturn(expected);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getConnection()).thenReturn(connection);

        assertThat(new CatalogResolver(databaseMetaData).resolveCatalog(null)).isEqualTo(expected);
    }

    @Test
    public void catalogIsNullFetchFromJDBCReturnsNullThenThrowsException() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getConnection()).thenReturn(connection);

        assertThatThrownBy(() ->new CatalogResolver(databaseMetaData).resolveCatalog(null))
                .isInstanceOf(InvalidConfigurationException.class)
                .hasMessageContaining("Catalog (-cat)")
                .hasMessageContaining("-cat %");
    }

}
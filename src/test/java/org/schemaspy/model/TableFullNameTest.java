/*
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2023 Nils Petzall
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
package org.schemaspy.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thomas Traude
 * @author Nils Petzall
 */
class TableFullNameTest {

    @ParameterizedTest
    @CsvSource(
            useHeadersInDisplayName = true,
            textBlock = """
                    db,     catalog,    schema,     table,      fullName
                    ,       ,           ,           ,           null.null
                    myDB,   myCatalog,  mySchema,   myTable,    myCatalog.mySchema.myTable
                    ,       myCatalog,  mySchema,   myTable,    myCatalog.mySchema.myTable
                    myDB,   ,           mySchema,   myTable,    mySchema.myTable
                    myDB,   myCatalog,  ,           myTable,    myCatalog.myTable
                    ,       myCatalog,  mySchema,   ,           myCatalog.mySchema.null
                    myDB,   ,           ,           myTable,    myDB.myTable
                    """
    )
    void testFullName(String db, String catalog, String schema, String table, String expectedFullName) {
        assertThat(Table.getFullName(db, catalog, schema, table)).isEqualTo(expectedFullName);
    }
}
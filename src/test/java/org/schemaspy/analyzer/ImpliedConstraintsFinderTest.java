/*
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2016 Ismail Simsek
 * Copyright (C) 2017 Thomas Traude
 * Copyright (C) 2017 Mårten Bohlin
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
package org.schemaspy.analyzer;

import org.junit.jupiter.api.Test;
import org.schemaspy.model.Database;
import org.schemaspy.model.ImpliedForeignKeyConstraint;
import org.schemaspy.model.Table;
import org.schemaspy.model.TableColumn;
import org.schemaspy.util.CaseInsensitiveMap;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ImpliedConstraintsFinderTest {
    private String catalog = "test";
    private String schema = "dbo";
    private Database database  = mock(Database.class);

    @Test
    void testGetImpliedConstraints() {
        Table album = createAlbumTable();
        Table track = createTrackTable();
        Table artist = createArtistTable();
        Table invoiceLine = createInvoiceLineTable();

        List<Table> tables = new ArrayList<>();
        tables.add(artist);
        tables.add(album);
        tables.add(track);
        tables.add(invoiceLine);

        ImpliedConstraintsFinder impliedConstraintsFinder = new ImpliedConstraintsFinder();
        List<ImpliedForeignKeyConstraint> impliedForeignKeyConstraintList = impliedConstraintsFinder.find(tables);

        ImpliedForeignKeyConstraint invoiceLineTrackId = new ImpliedForeignKeyConstraint(track.getColumn("Id"), invoiceLine.getColumn("TrackId"));
        ImpliedForeignKeyConstraint trackAlbumId = new ImpliedForeignKeyConstraint(album.getColumn("Id"), track.getColumn("AlbumId"));
        ImpliedForeignKeyConstraint albumArtistId = new ImpliedForeignKeyConstraint(artist.getColumn("Id"), album.getColumn("ArtistId"));

        assertThat(impliedForeignKeyConstraintList).containsExactlyInAnyOrder(invoiceLineTrackId, trackAlbumId, albumArtistId);
    }

    @Test
    void testGetImpliedConstraintsWithObscureTableAndColumnNames() {
        // Given
        Table parent = createTableWithObscureNamesParent();
        Table child = createTableWithObscureNamesChild1();

        List<Table> tables = new ArrayList<>();
        tables.add(parent);
        tables.add(child);
        ImpliedConstraintsFinder impliedConstraintsFinder = new ImpliedConstraintsFinder();

        // When
        List<ImpliedForeignKeyConstraint> impliedForeignKeyConstraintList = impliedConstraintsFinder.find(tables);

        // Then
        ImpliedForeignKeyConstraint obscureId = new ImpliedForeignKeyConstraint(parent.getColumn("{ColumnName}"), child.getColumn("ObscureParentTable{ColumnName}"));

        assertThat(impliedForeignKeyConstraintList).containsExactlyInAnyOrder(obscureId);
    }

    private Table createAlbumTable() {
        Table table = new Table(database, catalog, schema, "ALbum", "This is comment for database on PostgresSQL [Invoice] link is also working");
        TableColumn column1 = new TableColumn(table);
        column1.setName("ID");
        column1.setTypeName("int");
        column1.setLength(0);
        column1.setDetailedSize("10");

        TableColumn column2 = new TableColumn(table);
        column2.setName("Title");
        column2.setTypeName("varchar");
        column2.setType(12);
        column2.setLength(160);

        TableColumn column3 = new TableColumn(table);
        column3.setName("arTistID");
        column3.setTypeName("int");
        column3.setType(1);
        column3.setLength(0);
        column3.setDetailedSize("10");

        CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
        columns.put(column1.getName(), column1);
        columns.put(column2.getName(), column2);
        columns.put(column3.getName(), column3);
        table.setColumns(columns);

        table.setPrimaryColumn(column1);
        return table;
    }

    private Table createTrackTable() {
        Table table = new Table(database, catalog, schema, "Track", "");
        TableColumn column1 = new TableColumn(table);
        column1.setName("Id");
        column1.setType(1);
        column1.setTypeName("int");
        column1.setLength(0);
        column1.setDetailedSize("10");

        TableColumn column2 = new TableColumn(table);
        column2.setName("Name");
        column2.setTypeName("varchar");
        column2.setType(12);
        column2.setLength(200);

        TableColumn column3 = new TableColumn(table);
        column3.setName("AlbumId");
        column3.setTypeName("int");
        column3.setType(1);
        column3.setLength(0);
        column3.setDetailedSize("10");
        column3.setNullable(true);

        TableColumn column4 = new TableColumn(table);
        column4.setName("MediaTypeId");
        column4.setTypeName("int");
        column4.setType(1);
        column4.setLength(0);
        column4.setDetailedSize("10");

        TableColumn column5 = new TableColumn(table);
        column5.setName("GenreId");
        column5.setTypeName("int");
        column5.setType(1);
        column5.setLength(0);
        column5.setDetailedSize("10");
        column5.setNullable(true);

        TableColumn column6 = new TableColumn(table);
        column6.setName("Composer");
        column6.setTypeName("varchar");
        column6.setType(12);
        column6.setLength(220);
        column6.setNullable(true);

        TableColumn column7 = new TableColumn(table);
        column7.setName("Milliseconds");
        column7.setTypeName("int");
        column7.setType(1);
        column7.setLength(0);
        column7.setDetailedSize("10");

        TableColumn column8 = new TableColumn(table);
        column8.setName("Bytes");
        column8.setTypeName("int");
        column8.setType(1);
        column8.setLength(0);
        column8.setNullable(true);
        column8.setDetailedSize("10");

        TableColumn column9 = new TableColumn(table);
        column9.setName("UnitPrice");
        column9.setTypeName("numeric");
        column9.setType(10);
        column9.setLength(0);
        column9.setDetailedSize("10");

        CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
        columns.put(column1.getName(), column1);
        columns.put(column2.getName(), column2);
        columns.put(column3.getName(), column3);
        columns.put(column4.getName(), column4);
        columns.put(column5.getName(), column5);
        columns.put(column6.getName(), column6);
        columns.put(column7.getName(), column7);
        columns.put(column8.getName(), column8);
        columns.put(column9.getName(), column9);
        table.setColumns(columns);

        table.setPrimaryColumn(column1);
        return table;
    }

    private Table createArtistTable() {
        Table table = new Table(database, catalog, schema, "Artist", "");
        TableColumn column1 = new TableColumn(table);
        column1.setName("Id");
        column1.setTypeName("int");
        column1.setType(1);
        column1.setLength(0);
        column1.setDetailedSize("10");

        TableColumn column2 = new TableColumn(table);
        column2.setName("Name");
        column2.setTypeName("varchar");
        column2.setType(12);
        column2.setLength(120);

        CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
        columns.put(column1.getName(), column1);
        columns.put(column2.getName(), column2);
        table.setColumns(columns);

        table.setPrimaryColumn(column1);
        return table;
    }

    private Table createInvoiceLineTable() {
        Table table = new Table(database, catalog, schema, "InvoiceLine", "");
        TableColumn column1 = new TableColumn(table);
        column1.setName("Id");
        column1.setTypeName("int");
        column1.setType(1);
        column1.setLength(0);
        column1.setDetailedSize("10");

        TableColumn column2 = new TableColumn(table);
        column2.setName("InvoiceId");
        column2.setTypeName("int");
        column2.setType(1);
        column2.setLength(0);
        column2.setDetailedSize("10");

        TableColumn column3 = new TableColumn(table);
        column3.setName("TrackId");
        column3.setTypeName("int");
        column3.setType(1);
        column3.setLength(0);
        column3.setDetailedSize("10");

        TableColumn column4 = new TableColumn(table);
        column4.setName("UnitePrice");
        column4.setTypeName("numeric");
        column4.setType(10);
        column4.setLength(0);
        column4.setDetailedSize("10");

        TableColumn column5 = new TableColumn(table);
        column5.setName("Quantity");
        column5.setTypeName("int");
        column5.setType(1);
        column5.setLength(0);
        column5.setDetailedSize("10");

        CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
        columns.put(column1.getName(), column1);
        columns.put(column2.getName(), column2);
        columns.put(column3.getName(), column3);
        columns.put(column4.getName(), column4);
        columns.put(column5.getName(), column5);
        table.setColumns(columns);

        table.setPrimaryColumn(column1);
        return table;
    }

    private Table createTableWithObscureNamesParent() {
        Table table = new Table(database, catalog, schema, "ObscureParentTable", "");
        TableColumn column1 = new TableColumn(table);
        column1.setName("{ColumnName}");
        column1.setTypeName("varchar");
        column1.setType(12);
        column1.setLength(160);

        TableColumn column2 = new TableColumn(table);
        column2.setName("{ColumnName2}");
        column2.setTypeName("varchar");
        column2.setType(12);
        column2.setLength(160);

        CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
        columns.put(column1.getName(), column1);
        columns.put(column2.getName(), column2);
        table.setColumns(columns);

        table.setPrimaryColumn(column1);

        return table;
    }

    private Table createTableWithObscureNamesChild1() {
        Table table = new Table(database, catalog, schema, "Obscure{Child}Table", "");
        TableColumn column1 = new TableColumn(table);
        column1.setName("*()?@\",.#^$&/\\=");
        column1.setTypeName("varchar");
        column1.setType(12);
        column1.setLength(160);

        TableColumn column2 = new TableColumn(table);
        column2.setName("ObscureParentTable{ColumnName}");
        column2.setTypeName("varchar");
        column2.setType(12);
        column2.setLength(160);

        CaseInsensitiveMap<TableColumn> columns = new CaseInsensitiveMap<>();
        columns.put(column1.getName(), column1);
        columns.put(column2.getName(), column2);
        table.setColumns(columns);

        table.setPrimaryColumn(column1);

        return table;
    }
}

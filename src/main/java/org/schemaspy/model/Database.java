/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 * Copyright (C) 2016 Rafal Kasa
 * Copyright (C) 2017 Ismail Simsek
 * Copyright (C) 2017, 2018 Nils Petzaell
 * Copyright (C) 2017 Daniel Watt
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.model;

import org.schemaspy.util.CaseInsensitiveMap;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Ismail Simsek
 * @author Nils Petzaell
 * @author Daniel Watt
 *
 */
public class Database {

    private final DbmsMeta dbmsMeta;
    private final String databaseName;
    private final Catalog catalog ;
    private final Schema schema;
    private final Map<String, Table> tables = new CaseInsensitiveMap<>();
    private final Map<String, View> views = new CaseInsensitiveMap<>();
    private final Map<String, Table> remoteTables = new CaseInsensitiveMap<>(); // key: schema.tableName
    private final Map<String, Table> locals = new CombinedMap(tables, views);
    private final Map<String, Routine> routines = new CaseInsensitiveMap<>();
    private final ZonedDateTime connectTime = ZonedDateTime.now();

    public Database(
            DbmsMeta dbmsMeta,
            String name,
            String catalog,
            String schema
    ) {
        this.dbmsMeta = dbmsMeta;
        this.databaseName = name;
        this.catalog = new Catalog(catalog);
        this.schema = new Schema(schema);
    }

    public DbmsMeta getDbmsMeta() {
        return dbmsMeta;
    }

    public String getName() {
        return databaseName;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public Schema getSchema() {
        return schema;
    }

    /**
     * Details of the database type that's running under the covers.
     *
     * @return null if a description wasn't specified.
     */

    public Collection<Table> getTables() {
        return tables.values();
    }

    public Map<String, Table> getTablesMap() {
        return tables;
    }

    public Map<String, Table> getLocals() {
        return locals;
    }

    public Collection<View> getViews() {
        return views.values();
    }

    public Map<String, View> getViewsMap() {
        return views;
    }

    public Collection<Table> getRemoteTables() {
        return remoteTables.values();
    }

    public Map<String, Table> getRemoteTablesMap() {
        return remoteTables;
    }

    public Collection<Routine> getRoutines() {
        return routines.values();
    }

    public Map<String, Routine> getRoutinesMap() {
        return routines;
    }

    /**
     * Used in Mustache template
     * @return creation time of model
     */
    public String getConnectTime() {
        return connectTime.format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm z yyyy"));
    }

    public String getDatabaseProduct() {
        return dbmsMeta.getProductName() + " - " + dbmsMeta.getProductVersion();
    }

    /**
     * Returns a 'key' that's used to identify a remote table
     * in the remoteTables map.
     *
     * @param cat
     * @param sch
     * @param table
     * @return
     */
    public String getRemoteTableKey(String cat, String sch, String table) {
        return Table.getFullName(getName(), cat, sch, table);
    }

}

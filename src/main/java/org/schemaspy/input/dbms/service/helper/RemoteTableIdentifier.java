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
package org.schemaspy.input.dbms.service.helper;

import org.schemaspy.input.dbms.xml.ForeignKeyMeta;
import org.schemaspy.input.dbms.xml.TableMeta;

public class RemoteTableIdentifier {

    public static RemoteTableIdentifier from(ExportForeignKey foreignKey) {
        return new RemoteTableIdentifier(
                foreignKey.getFkTableCat(),
                foreignKey.getFkTableSchema(),
                foreignKey.getFkTableName()
        );
    }

    public static RemoteTableIdentifier from(ImportForeignKey foreignKey) {
        return new RemoteTableIdentifier(
                foreignKey.getPkTableCat(),
                foreignKey.getPkTableSchema(),
                foreignKey.getPkTableName()
        );
    }

    public static RemoteTableIdentifier from(TableMeta tableMeta) {
        return new RemoteTableIdentifier(
                tableMeta.getRemoteCatalog(),
                tableMeta.getRemoteSchema(),
                tableMeta.getName()
        );
    }

    public static RemoteTableIdentifier from(ForeignKeyMeta foreignKeyMeta) {
        return new RemoteTableIdentifier(
                foreignKeyMeta.getRemoteCatalog(),
                foreignKeyMeta.getRemoteSchema(),
                foreignKeyMeta.getTableName()
        );
    }

    private final String catalogName;
    private final String schemaName;
    private final String tableName;

    public RemoteTableIdentifier(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }
}

/*
 * Copyright (C) 2016 Rafal Kasa
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
package org.schemaspy.view;
import org.schemaspy.model.TableIndex;

/**
 * Created by rkasa on 2016-03-23.
 *
 * @author Rafal Kasa
 * @author Daniel Watt
 */
public class MustacheTableIndex {

    private TableIndex index;

    public MustacheTableIndex(TableIndex index) {
        this.index = index;
    }

    public TableIndex getIndex() {
        return index;
    }

    public String getKey() {
        String keyType;

        if (index.isPrimaryKey()) {
            keyType = " class='primaryKey' title='Primary Key'";
        } else if (index.isUnique()) {
            keyType = " class='uniqueKey' title='Unique Key'";
        } else {
            keyType = " title='Indexed'";
        }
        return keyType;
    }

    public String getKeyIcon() {
        String keyIcon = "";
        if (index.isPrimaryKey() || index.isUnique()) {
            keyIcon = "<i class='icon ion-key iconkey'></i> ";
        }

        return  keyIcon;
    }

}


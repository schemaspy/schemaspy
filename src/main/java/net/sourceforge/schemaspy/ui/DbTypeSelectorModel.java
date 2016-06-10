/*
 * This file is a part of the SchemaSpy project (http://schemaspy.sourceforge.net).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010 John Currier
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
package net.sourceforge.schemaspy.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.util.DbSpecificConfig;

/**
 * @author John Currier
 */
public class DbTypeSelectorModel extends AbstractListModel implements ComboBoxModel {
    private static final long serialVersionUID = 1L;
    private final List<DbSpecificConfig> dbConfigs = new ArrayList<DbSpecificConfig>();
    private Object selected;

    public DbTypeSelectorModel(String defaultType) {
        Pattern pattern = Pattern.compile(".*/" + defaultType);
        Set<String> dbTypes = new TreeSet<String>(Config.getBuiltInDatabaseTypes(Config.getLoadedFromJar()));
        for (String dbType : dbTypes) {
            DbSpecificConfig config = new DbSpecificConfig(dbType);
            dbConfigs.add(config);

            if (pattern.matcher(dbType).matches()) {
                setSelectedItem(config);
            }
        }

        if (getSelectedItem() == null && dbConfigs.size() > 0)
            setSelectedItem(dbConfigs.get(0));
    }

    /* (non-Javadoc)
     * @see javax.swing.ComboBoxModel#getSelectedItem()
     */
    public Object getSelectedItem() {
        return selected;
    }

    /* (non-Javadoc)
     * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
     */
    public void setSelectedItem(Object anItem) {
        selected = anItem;
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
        return dbConfigs.get(index);
    }

    /* (non-Javadoc)
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
        return dbConfigs.size();
    }
}


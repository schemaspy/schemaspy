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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import net.sourceforge.schemaspy.Config;
import net.sourceforge.schemaspy.Config.MissingRequiredParameterException;
import net.sourceforge.schemaspy.util.DbSpecificConfig;
import net.sourceforge.schemaspy.util.DbSpecificOption;

/**
 * @author John Currier
 */
public class DbConfigTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private final List<PropertyDescriptor> options = new ArrayList<PropertyDescriptor>();
    private Config config = Config.getInstance(); // the config associated with DbSpecificConfig

    public DbConfigTableModel() {
        PropertyDescriptor[] props = getConfigProps();

        options.add(getDescriptor("outputDir", "Directory to generate HTML output to", props));
        options.add(getDescriptor("schema", "Schema to evaluate", props));
        options.add(getDescriptor("user", "User ID to connect with", props));
        options.add(getDescriptor("password", "Password associated with user id", props));
        options.add(getDescriptor("impliedConstraintsEnabled", "XXXX", props));
    }

    public void setDbSpecificConfig(DbSpecificConfig dbConfig) {
        config  = dbConfig.getConfig();
        Config.setInstance(config);
        PropertyDescriptor[] props = getConfigProps();
        removeDbSpecificOptions();

        for (DbSpecificOption option : dbConfig.getOptions()) {
            PropertyDescriptor descriptor = getDescriptor(option.getName(), option.getDescription(), props);
            descriptor.setValue("dbSpecific", Boolean.TRUE);
            options.add(descriptor);
        }

        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Option";
            default:
                return "Value";
        }
    }

    /**
     * @param string
     * @param string2
     * @param props
     * @return
     */
    private PropertyDescriptor getDescriptor(String propName, String description, PropertyDescriptor[] props) {
        if (props == null)
            props = getConfigProps();

        for (int i = 0; i < props.length; ++i) {
            PropertyDescriptor prop = props[i];
            if (prop.getName().equalsIgnoreCase(propName)) {
                prop.setShortDescription(description);
                return prop;
            }
        }

        throw new IllegalArgumentException(propName + " is not a valid configuration item");
    }

    private PropertyDescriptor[] getConfigProps() throws RuntimeException {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(Config.class);
        } catch (IntrospectionException exc) {
            throw new RuntimeException(exc);
        }

        return beanInfo.getPropertyDescriptors();
    }

    private void removeDbSpecificOptions() {
        Iterator<PropertyDescriptor> iter = options.iterator();
        while (iter.hasNext()) {
            PropertyDescriptor descriptor = iter.next();
            if (descriptor.getValue("dbSpecific") != null)
                iter.remove();
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 2;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return options.size();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col != 1)
            return false;

        return options.get(row).getWriteMethod() != null;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int column) {
        PropertyDescriptor descriptor = options.get(row);
        switch (column) {
            case 0:
                return descriptor.getName();
            case 1:
                try {
                    Object value = descriptor.getReadMethod().invoke(config, (Object[])null);
                    //System.out.println(descriptor.getReadMethod().getName() + ":'" + value + "' " + (value != null ? value.getClass().toString() : ""));
                    return value;
                } catch (InvocationTargetException exc) {
                    if (exc.getCause() instanceof MissingRequiredParameterException)
                        return null;
                    throw new RuntimeException(exc);
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
        }

        return null;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Object oldValue = getValueAt(row, col);
        if (oldValue != value && (value == null || oldValue == null || !value.equals(oldValue))) {
            PropertyDescriptor descriptor = options.get(row);
            try {
                //System.out.println(descriptor.getWriteMethod().getName() + ":'" + value + "' " + (value != null ? value.getClass().toString() : ""));
                if (value instanceof String && descriptor.getPropertyType().isAssignableFrom(Integer.class)) {
                    try {
                        value = Integer.valueOf((String)value);
                    } catch (NumberFormatException nfe) {
                        value = oldValue;
                    }
                }

                descriptor.getWriteMethod().invoke(config, new Object[] {value});
            } catch (Exception exc) {
                throw new RuntimeException(exc);
            }

            fireTableCellUpdated(row, col);
        }
    }

    /**
     * @param row
     * @return
     */
    public Class<?> getClass(int row) {
        PropertyDescriptor descriptor = options.get(row);
        return descriptor.getPropertyType();
    }

    /**
     * @param row
     * @return
     */
    public String getDescription(int row) {
        PropertyDescriptor descriptor = options.get(row);
        return descriptor.getShortDescription();
    }
}
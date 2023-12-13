/*
 * Copyright (C) 2004 - 2011 John Currier
 * Copyright (C) 2016, 2017 Rafal Kasa
 * Copyright (C) 2017 Wojciech Kasa
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
package org.schemaspy.input.dbms;

import org.schemaspy.input.dbms.classloader.ClClasspath;
import org.schemaspy.input.dbms.classpath.GetExistingUrls;
import org.schemaspy.input.dbms.driver.Driversource;
import org.schemaspy.input.dbms.driver.DsCached;
import org.schemaspy.input.dbms.driver.DsDriverClass;
import org.schemaspy.input.dbms.driverclass.DcFacade;

import java.sql.Driver;
import org.schemaspy.input.dbms.driverpath.Driverpath;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Wojciech Kasa
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class DbDriverLoader implements Driversource {

    private final String[] driverClass;
    private Driverpath driverPath;

    public DbDriverLoader(final String[] driverClass, final Driverpath driverPath) {
        this.driverClass = driverClass;
        this.driverPath = driverPath;
    }

    /**
     * Returns an instance of {@link Driver} specified by <code>driverClass</code>
     * loaded from <code>driverPath</code>.
     *
     * @return
     */
    public synchronized Driver driver() {
        Class<Driver> driverClass = new DcFacade(
            this.driverClass,
            new ClClasspath(
                new GetExistingUrls(this.driverPath)
            )
        ).value();

        // @see DriverManager.setLogStream(PrintStream)
        //TODO implement PrintStream to Logger bridge.
        // setLogStream should only be called once maybe in Main
        return new DsCached(
            driverClass,
            new DsDriverClass(driverClass)
        ).driver();
    }
}

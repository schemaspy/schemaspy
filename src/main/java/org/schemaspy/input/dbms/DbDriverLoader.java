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

import org.schemaspy.connection.*;
import org.schemaspy.input.dbms.classloader.ClClasspath;
import org.schemaspy.input.dbms.classpath.GetExistingUrls;
import org.schemaspy.input.dbms.driver.DsCached;
import org.schemaspy.input.dbms.driver.DsDriverClass;
import org.schemaspy.input.dbms.driverclass.DcFacade;
import org.schemaspy.input.dbms.driverpath.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.Driver;
import java.util.*;

/**
 * @author John Currier
 * @author Rafal Kasa
 * @author Wojciech Kasa
 * @author Nils Petzaell
 * @author Daniel Watt
 */
public class DbDriverLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Connection con;
    private final ConnectionURLBuilder urlBuilder;
    private final String[] driverClass;
    private Driverpath driverPath;

    public DbDriverLoader(final ConnectionConfig connectionConfig) {
        this(connectionConfig, new ConnectionURLBuilder(connectionConfig));
    }

    public DbDriverLoader(final ConnectionConfig connectionConfig, final ConnectionURLBuilder urlBuilder) {
        this(connectionConfig, urlBuilder, connectionConfig.getDatabaseTypeProperties());
    }

    public DbDriverLoader(
        final ConnectionConfig connectionConfig,
        final ConnectionURLBuilder urlBuilder,
        final Properties properties
    ) {
        this(
            connectionConfig,
            urlBuilder,
            properties.getProperty("driver").split(","),
            new DpMissingPathChecked(
                new DpFallback(
                    new DpConnectionConfig(connectionConfig),
                    new DpFallback(
                        new DpProperties(properties),
                        new DpNull()
                    )
                )
            )
        );
    }

    public DbDriverLoader(
        final ConnectionConfig connectionConfig,
        final ConnectionURLBuilder urlBuilder,
        final String[] driverClass,
        final Driverpath driverPath
    ) {
        this(
            new WithPassword(
                connectionConfig.getPassword(),
                new WithUser(
                    connectionConfig.getUser(),
                    new PreferencesConnection(connectionConfig.getConnectionProperties())
                )
            ),
            urlBuilder,
            driverClass,
            driverPath
        );
    }

    public DbDriverLoader(
            final Connection con,
            final ConnectionURLBuilder urlBuilder,
            final String[] driverClass,
            final Driverpath driverPath
    ) {
        this.con = con;
        this.urlBuilder = urlBuilder;
        this.driverClass = driverClass;
        this.driverPath = driverPath;
    }

    public java.sql.Connection getConnection() throws IOException {
        return new SqlConnection(this.con, this.urlBuilder, this.driverClass, this::getDriver).connection();
    }

    /**
     * Returns an instance of {@link Driver} specified by <code>driverClass</code>
     * loaded from <code>driverPath</code>.
     *
     * @return
     */
    protected synchronized Driver getDriver() {
        String[] driverClasses = this.driverClass;
        String driverPath = this.driverPath.value();

        Class<Driver> driverClass = new DcFacade(
            driverClasses,
            new ClClasspath(
                new GetExistingUrls(driverPath)
            )
        ).value();

        // @see DriverManager.setLogStream(PrintStream)
        //TODO implement PrintStream to Logger bridge.
        // setLogStream should only be called once maybe in Main
        return new DsCached(
            driverClasses,
            driverPath,
            driverClass,
            new DsDriverClass(driverClass)
        ).driver();
    }
}

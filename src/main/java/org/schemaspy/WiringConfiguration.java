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
package org.schemaspy;

import org.schemaspy.cli.CommandLineArguments;
import org.schemaspy.input.dbms.service.DatabaseService;
import org.schemaspy.input.dbms.service.SqlService;
import org.schemaspy.input.dbms.service.TableService;
import org.schemaspy.input.dbms.service.ViewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * @author Nils Petzaell
 */
@Configuration
public class WiringConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public SqlService sqlService() {
        return new SqlService();
    }

    @Bean
    public TableService tableService(SqlService sqlService) {
        return new TableService(sqlService);
    }

    @Bean
    public ViewService viewService(SqlService sqlService) {
        return new ViewService(sqlService);
    }

    @Bean
    public DatabaseService databaseService(Clock clock, TableService tableService, ViewService viewService, SqlService sqlService) {
        return new DatabaseService(clock, tableService, viewService, sqlService);
    }

    @Bean
    public SchemaAnalyzer schemaAnalyzer(SqlService sqlService, DatabaseService databaseService, CommandLineArguments commandLineArguments) {
        return new SchemaAnalyzer(sqlService, databaseService, commandLineArguments);
    }

}

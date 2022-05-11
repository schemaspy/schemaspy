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
import org.schemaspy.input.dbms.service.SqlService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nils Petzaell
 */
@Configuration
public class WiringConfiguration {

    @Bean
    public SqlService sqlService() {
        return new SqlService();
    }

    @Bean
    public SchemaAnalyzer schemaAnalyzer(SqlService sqlService, CommandLineArguments commandLineArguments) {
        return new SchemaAnalyzer(sqlService, commandLineArguments);
    }

}

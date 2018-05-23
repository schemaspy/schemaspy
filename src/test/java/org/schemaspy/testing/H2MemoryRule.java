/*
 * Copyright (C) 2017 Nils Petzaell
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
package org.schemaspy.testing;

import org.h2.Driver;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Nils Petzaell
 */
public class H2MemoryRule extends ExternalResource {

  private final String connectionString;
  private String scriptPath;
  private List<String> sqls = new ArrayList<>();

  private Connection keepAlive;

  public H2MemoryRule(String name) {
    this.connectionString = "jdbc:h2:mem:" + name;
  }

  public H2MemoryRule addSqls(String...sqls) {
    Arrays.stream(sqls).forEach(s -> this.sqls.add(s));
    return this;
  }

  public H2MemoryRule addSqlScript(String scriptPath) {
    this.scriptPath = scriptPath;
    return this;
  }

  public String getConnectionURL() {
    return connectionString;
  }

  @Override
  protected void before() throws Throwable {
    loadScript();
    Driver.load();
    String user = "sa";
    keepAlive = DriverManager.getConnection(connectionString, "sa", "");
    if (!sqls.isEmpty()) {
      Statement statement = keepAlive.createStatement();
      for (String sql : sqls) {
        statement.addBatch(sql.trim());
      }
      statement.executeBatch();
      keepAlive.commit();
    }
  }

  private void loadScript() throws IOException {
    if (Objects.nonNull(scriptPath) && !scriptPath.trim().isEmpty()) {
      Path p = Paths.get(scriptPath);
      String[] sqlStatements = Files.readAllLines(p).stream().map(String::trim).collect(Collectors.joining()).split(";");
      Arrays.stream(sqlStatements).forEach(s -> this.sqls.add(s + ";"));
    }
  }

  public Connection getConnection() {
    return keepAlive;
  }

  @Override
  protected void after() {
    try {
      if (keepAlive != null && !keepAlive.isClosed()) {
        keepAlive.close();
      }
    } catch (SQLException ignore) {
      //
    }
  }
}
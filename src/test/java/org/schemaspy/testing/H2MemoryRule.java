package org.schemaspy.testing;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.Driver;
import org.junit.rules.ExternalResource;

public class H2MemoryRule extends ExternalResource {

  private final String connectionString;
  private final String[] sqls;

  private Connection keepAlive;

  public H2MemoryRule(String name, String...sqls) {
    this.connectionString = "jdbc:h2:mem:" + name;
    this.sqls = sqls;
  }

  public String getConnectionURL() {
    return connectionString;
  }

  @Override
  protected void before() throws Throwable {
    Driver.load();
    String user = "sa";
    keepAlive = DriverManager.getConnection(connectionString, "sa", "");
    Statement statement = keepAlive.createStatement();
    for (String sql : sqls) {
      statement.addBatch(sql);
    }
    statement.executeBatch();
    keepAlive.commit();
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

package org.schemaspy.service;

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rkasa on 2016-12-10.
 */
@Service
public class ViewService {

    @Autowired
    SqlService sqlService;

    private final static Logger logger = Logger.getLogger(TableService.class.getName());

    /**
     * Extract the SQL that describes this view from the database
     *
     * @return
     * @throws SQLException
     */
    public String fetchViewSql(Database db, View view) throws SQLException {
        String selectViewSql = Config.getInstance().getDbProperties().getProperty("selectViewSql");
        if (selectViewSql == null)
            return null;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder viewDefinition = new StringBuilder();
        try {
            stmt = sqlService.prepareStatement(selectViewSql, view.getName());
            rs = stmt.executeQuery();
            while (rs.next()) {
                try {
                    viewDefinition.append(rs.getString("view_definition"));
                } catch (SQLException tryOldName) {
                    viewDefinition.append(rs.getString("text"));
                }
            }
            return viewDefinition.toString();
        } catch (SQLException sqlException) {
            System.err.println(selectViewSql);
            throw sqlException;
        } finally {
            if (rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
    }
}

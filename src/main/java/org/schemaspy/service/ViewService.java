package org.schemaspy.service;

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.View;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by rkasa on 2016-12-10.
 */
@Service
public class ViewService {

    private final SqlService sqlService;

    private static final Logger LOGGER = Logger.getLogger(ViewService.class.getName());

    public ViewService(SqlService sqlService) {
        this.sqlService = Objects.requireNonNull(sqlService);
    }

    /**
     * Extract the SQL that describes this view from the database
     *
     * @return
     * @throws SQLException
     */
    public String fetchViewSql(Database db, View view) throws SQLException {
        String selectViewSql = Config.getInstance().getDbProperties().getProperty("selectViewSql");
        if (selectViewSql == null) {
            return null;
        }

        StringBuilder viewDefinition = new StringBuilder();
        try (PreparedStatement stmt = sqlService.prepareStatement(selectViewSql, db, view.getName());
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    viewDefinition.append(rs.getString("view_definition"));
                } catch (SQLException tryOldName) {
                    viewDefinition.append(rs.getString("text"));
                }
            }
            return viewDefinition.toString();
        } catch (SQLException sqlException) {
            LOGGER.log(Level.SEVERE, selectViewSql);
            throw sqlException;
        }
    }
}

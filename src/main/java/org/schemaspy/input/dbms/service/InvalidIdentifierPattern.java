package org.schemaspy.input.dbms.service;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class InvalidIdentifierPattern {
    private final DatabaseMetaData databaseMetaData;

    public InvalidIdentifierPattern(final DatabaseMetaData databaseMetaData) {
        this.databaseMetaData = databaseMetaData;
    }

    /**
     * Return a <code>Pattern</code> whose matcher will return <code>true</code>
     * when run against an identifier that contains a character that is not
     * acceptable by the database without being quoted.
     */
    public Pattern pattern() throws SQLException {
        StringBuilder validChars = new StringBuilder("a-zA-Z0-9_");
        String reservedRegexChars = "-&^";
        String extraValidChars = databaseMetaData.getExtraNameCharacters();
        for (int i = 0; i < extraValidChars.length(); ++i) {
            char ch = extraValidChars.charAt(i);
            if (reservedRegexChars.indexOf(ch) >= 0) {
                validChars.append("" + "\\");
            }
            validChars.append(ch);
        }
        return Pattern.compile("[^" + validChars + "]");
    }
}

package org.schemaspy.input.dbms.service.name;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.schemaspy.model.DbmsMeta;
import org.schemaspy.util.naming.Name;

/**
 * Ensures that SQL identifiers are quoted appropriately.
 */
public class Sanitized implements Name {
    private Pattern invalidIdentifierPattern;
    private DbmsMeta dbms;
    private final Name origin;

    public Sanitized(
        final Pattern invalidIdentifierPattern,
        final DbmsMeta dbms,
        final Name origin
    ) {
        this.invalidIdentifierPattern = invalidIdentifierPattern;
        this.dbms = dbms;
        this.origin = origin;
    }

    @Override
    public String value() {
        final String value = this.origin.value();

        // look for any character that isn't valid (then matcher.find() returns true)
        Matcher matcher = this.invalidIdentifierPattern.matcher(value);

        boolean quotesRequired = matcher.find() || this.dbms.reservedWords().contains(value);

        // name contains something that must be quoted
        final Name result = quotesRequired
            ? new DatabaseQuoted(this.dbms, this.origin)
            : this.origin;

        return result.value();
    }
}

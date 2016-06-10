package net.sourceforge.schemaspy.view;
import net.sourceforge.schemaspy.model.ForeignKeyConstraint;
import net.sourceforge.schemaspy.model.TableColumn;
import net.sourceforge.schemaspy.model.TableIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by rkasa on 2016-03-23.
 */
public class MustacheTableIndex {

    private TableIndex index;

    public MustacheTableIndex(TableIndex index) {
        this.index = index;
    }

    public TableIndex getIndex() {
        return index;
    }

    public String getKey() {
        String keyType = "";

        if (index.isPrimaryKey()) {
            keyType = " class='primaryKey' title='Primary Key'";
        } else if (index.isUnique()) {
            keyType = " class='uniqueKey' title='Unique Key'";
        } else {
            keyType = " title='Indexed'";
        }
        return keyType;
    }

    public String getKeyIcon() {
        String keyIcon = "";
        if (index.isPrimaryKey() || index.isUnique()) {
            keyIcon = "<i class='icon ion-key iconkey'></i> ";
        }

        return  keyIcon;
    }

}


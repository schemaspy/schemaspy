package org.schemaspy.view;
import org.schemaspy.model.TableIndex;

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
        String keyType;

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


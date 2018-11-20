package org.schemaspy.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ben Hartwich
 */
public class TriggerTest {
    @Test
    public void constructor() {
        String actionStatement = "BEGIN       \n" +
                "\t\t  INSERT INTO\n" +
                "\t\t\tOM_CMN_OrderItem\n" +
                "\t\t(\n" +
                "\t\t\t`orderId`, entityId, `itemSubTypeRefId`, `locationId`, `recordTypeRefId`, `parentOrderItemId`, `omId`, hideSummaryRowFlag, updatedOnDTime ,\tidx, mainLineItemFlag\n" +
                "\t\t)\t\t\t\t\t\n" +
                "\t\tSELECT\n" +
                "\t\t\titem.`orderId`, NEW.id, subtype.id, o.`locationId`, item.recordTypeRefId, `parentOrderItemId`, o.`omId`, true, CURRENT_TIMESTAMP,0,0\n" +
                "\t\tFROM\n" +
                "\t\t\tOM_CMN_OrderItem item\t\t\t\t\t\n" +
                "\t\tINNER JOIN\n" +
                "\t\t\tOM_CMN_R_ItemSubTypeRef subtype\n" +
                "\t\tON\n" +
                "\t\t\tsubtype.name = 'NPD Fibre'\t\t\n" +
                "\t\tINNER JOIN\n" +
                "\t\t\tOM_CMN_Order o\n" +
                "\t\tON\n" +
                "\t\t\to.id = item.orderId\n" +
                "\t\tWHERE\n" +
                "\t\t\titem.id = NEW.itemId;\n" +
                "    END";
        Trigger t = new Trigger(
                "myDb",
                "myTrigger",
                actionStatement,
                "myTable",
                "AFTER",
                "INSERT"
        );

        assertThat(t.getSchema()).isEqualTo("myDb");
        assertThat(t.getName()).isEqualTo("myTrigger");
        assertThat(t.getActionStatement()).isEqualTo(actionStatement);
        assertThat(t.getTableName()).isEqualTo("myTable");
        assertThat(t.getActionTiming()).isEqualTo("AFTER");
        assertThat(t.getEventManipulation()).isEqualTo("INSERT");
    }

}
/*
 * Copyright (C) 2017 Rafal Kasa
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
 * along with Foobar. If not, see <http://www.gnu.org/licenses/>.
 */
package org.schemaspy.model;

/**
 * Created by rkasa on 2017-02-27.
 * @author Rafal Kasa
 */
public class ForeignKey {
    String FK_NAME;
    String FKCOLUMN_NAME;
    String FKTABLE_CAT;
    String FKTABLE_SCHEM;
    String FKTABLE_NAME;
    String PKTABLE_CAT;
    String PKTABLE_SCHEM;
    String PKTABLE_NAME;
    String PKCOLUMN_NAME;
    Integer UPDATE_RULE;
    Integer DELETE_RULE;

    public String getFK_NAME() {
        return FK_NAME;
    }

    public void setFK_NAME(String FK_NAME) {
        this.FK_NAME = FK_NAME;
    }

    public String getFKCOLUMN_NAME() {
        return FKCOLUMN_NAME;
    }

    public void setFKCOLUMN_NAME(String FKCOLUMN_NAME) {
        this.FKCOLUMN_NAME = FKCOLUMN_NAME;
    }

    public String getFKTABLE_CAT() {
        return FKTABLE_CAT;
    }

    public void setFKTABLE_CAT(String FKTABLE_CAT) {
        this.FKTABLE_CAT = FKTABLE_CAT;
    }

    public String getFKTABLE_SCHEM() {
        return FKTABLE_SCHEM;
    }

    public void setFKTABLE_SCHEM(String FKTABLE_SCHEM) {
        this.FKTABLE_SCHEM = FKTABLE_SCHEM;
    }

    public String getFKTABLE_NAME() {
        return FKTABLE_NAME;
    }

    public void setFKTABLE_NAME(String FKTABLE_NAME) {
        this.FKTABLE_NAME = FKTABLE_NAME;
    }

    public String getPKTABLE_CAT() {
        return PKTABLE_CAT;
    }

    public void setPKTABLE_CAT(String PKTABLE_CAT) {
        this.PKTABLE_CAT = PKTABLE_CAT;
    }

    public String getPKTABLE_SCHEM() {
        return PKTABLE_SCHEM;
    }

    public void setPKTABLE_SCHEM(String PKTABLE_SCHEM) {
        this.PKTABLE_SCHEM = PKTABLE_SCHEM;
    }

    public String getPKTABLE_NAME() {
        return PKTABLE_NAME;
    }

    public void setPKTABLE_NAME(String PKTABLE_NAME) {
        this.PKTABLE_NAME = PKTABLE_NAME;
    }

    public String getPKCOLUMN_NAME() {
        return PKCOLUMN_NAME;
    }

    public void setPKCOLUMN_NAME(String PKCOLUMN_NAME) {
        this.PKCOLUMN_NAME = PKCOLUMN_NAME;
    }

    public Integer getUPDATE_RULE() {
        return UPDATE_RULE;
    }

    public void setUPDATE_RULE(Integer UPDATE_RULE) {
        this.UPDATE_RULE = UPDATE_RULE;
    }

    public Integer getDELETE_RULE() {
        return DELETE_RULE;
    }

    public void setDELETE_RULE(Integer DELETE_RULE) {
        this.DELETE_RULE = DELETE_RULE;
    }
}

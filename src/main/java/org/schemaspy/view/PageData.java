/*
 * Copyright (C) 2018 Nils Petzaell
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
package org.schemaspy.view;

import java.util.HashMap;
import java.util.Map;

public class PageData {
    private String templateName;
    private Map<String, Object> scope = new HashMap<>();
    private String scriptName = "";
    private int depth = 0;

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, Object> getScope() {
        return scope;
    }

    public String getScriptName() {
        return scriptName;
    }

    public int getDepth() {
        return depth;
    }

    public static class Builder {

        private PageData pageData = new PageData();

        public Builder templateName(String templateName) {
            pageData.templateName = templateName;
            return this;
        }

        public Builder addToScope(String key, Object value) {
            pageData.scope.put(key, value);
            return this;
        }

        public Builder scriptName(String scriptName) {
            pageData.scriptName = scriptName;
            return this;
        }

        public Builder depth(int depth) {
            pageData.depth = depth;
            return this;
        }

        public PageData getPageData() {
            return pageData;
        }
    }
}
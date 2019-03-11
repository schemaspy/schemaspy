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
package org.schemaspy.input.dbms.service;

public class DurationFormatter {

    private static final long MS_TO_SEC = 1000;
    private static final long MS_TO_MIN = MS_TO_SEC * 60;
    private static final long MS_TO_HR = MS_TO_MIN * 60;

    private DurationFormatter() {}

    public static String formatMS(final long durationInMilliseconds) {
        long timeToProcess = durationInMilliseconds;
        StringBuilder stringBuilder = new StringBuilder();
        if (timeToProcess >= MS_TO_HR) {
            stringBuilder.append(timeToProcess/MS_TO_HR).append( " hr ");
            timeToProcess = timeToProcess % MS_TO_HR;
        }
        if (timeToProcess >= MS_TO_MIN) {
            stringBuilder.append(timeToProcess/MS_TO_MIN).append(" min ");
            timeToProcess = timeToProcess % MS_TO_MIN;
        }
        if (timeToProcess >= MS_TO_SEC) {
            stringBuilder.append(timeToProcess/MS_TO_SEC).append(" s ");
            timeToProcess = timeToProcess % MS_TO_SEC;
        }
        if (timeToProcess >  0) {
            stringBuilder.append(timeToProcess).append(" ms");
        }
        return stringBuilder.toString().trim();
    }
}

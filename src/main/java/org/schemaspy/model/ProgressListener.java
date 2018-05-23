/*
 * Copyright (C) 2004 - 2011, 2014 John Currier
 *
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 *
 * SchemaSpy is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * SchemaSpy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.schemaspy.model;

import org.schemaspy.Config;

import java.util.Collection;

/**
 * Listener of schema analysis and ERD generation progress.
 * Overall intent is to allow various views to render progress details appropriately.
 *
 * @see ConsoleProgressListener
 * @author John Currier
 */
public interface ProgressListener {
	/**
	 * @return startup / connection duration in milliseconds
	 */
	long startedGatheringDetails();
	void gatheringDetailsProgressed(Table table);

	/**
	 * @return detail gathering duration in milliseconds
	 */
	long startedConnectingTables();
	void connectingTablesProgressed(Table table);

	/**
	 * @return table connection duration in milliseconds
	 */
	long startedGraphingSummaries();
	void graphingSummaryProgressed();

	/**
	 * @return summary graphing duration in milliseconds
	 */
	long startedGraphingDetails();
	void graphingDetailsProgressed(Table table);

	/**
	 * @return detail graphing duration in milliseconds
	 */
	long finishedGatheringDetails();

	/**
	 * @return overall duration duration in milliseconds
	 */
	long finished(Collection<Table> tables, Config config);

	/**
	 * Ran into a recoverable exception. Returns a string to be logged or <code>null</code> if it shouldn't be logged.
	 * @param msg
	 * @param exc
	 * @param sql
	 * @return
	 */
	public String recoverableExceptionEncountered(String msg, Exception exc, String sql);
}
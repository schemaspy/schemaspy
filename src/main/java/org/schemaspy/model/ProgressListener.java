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

import java.util.Collection;

/**
 * Listener of schema analysis and ERD generation progress.
 * Overall intent is to allow various views to render progress details appropriately.
 *
 * @author John Currier
 */
public interface ProgressListener {

	void startCollectingTablesViews();
	void tableViewCollected(Table table);
	/**
	 * @return duration of CollectingTablesViews in milliseconds
	 */
	long finishedCollectingTablesViews();

	void startConnectingTablesViews();
	void connectedTableView(Table table);
	/**
	 * @return duration of ConnectingTablesViews in milliseconds
	 */
	long finishedConnectingTablesViews();

	void startCreatingSummaries();
	void createdSummary();
	/**
	 * @return duration of CreatingSummaries in milliseconds
	 */
	long finishedCreatingSummaries();

	void startCreatingTablePages();
	void createdTablePage(Table table);

	/**
	 * @return duration of CreatingTablePages in milliseconds
	 */
	long finishedCreatingTablePages();

	/**
	 * @return overall duration in milliseconds
	 */
	long finished(Collection<Table> tables);
}
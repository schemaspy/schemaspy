/*
 * This file is a part of the SchemaSpy project (http://schemaspy.org).
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2014 John Currier
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
import org.schemaspy.cli.CommandLineArguments;

import java.io.File;
import java.util.Collection;

/**
 * Implementation of {@link ProgressListener} that sends its output to the console.
 *
 * @author John Currier
 */
public class ConsoleProgressListener implements ProgressListener {
	private final CommandLineArguments commandLineArguments;

	private final boolean render;
	private long startedAt;
	private long startedGatheringAt;
	private long startedConnectingAt;
	private long startedGraphingSummariesAt;
	private long startedGraphingDetailsAt;
	private long finishedAt;

    public ConsoleProgressListener(boolean render, CommandLineArguments commandLineArguments) {
    	this.render = render;
		this.commandLineArguments = commandLineArguments;
		startedAt = System.currentTimeMillis();
	}

	@Override
	public long startedGatheringDetails() {
        startedGatheringAt = System.currentTimeMillis();
        long duration = startedAt - startedGatheringAt;

        if (render) {
			System.out.print("Gathering schema details..."); //NOSONAR
		}

        return duration;
	}

	@Override
	public void gatheringDetailsProgressed(Table table) {
		if (render) {
			System.out.print('.'); //NOSONAR
		}
	}

	@Override
	public long startedConnectingTables() {
        startedConnectingAt = System.currentTimeMillis();
        long duration = startedConnectingAt - startedGatheringAt;

        if (render) {
	        System.err.flush(); //NOSONAR
	        System.out.flush(); //NOSONAR
	        System.out.println("(" + duration / 1000 + "sec)"); //NOSONAR
	        System.out.print("Connecting relationships..."); //NOSONAR
        }

        return duration;
	}

	@Override
	public void connectingTablesProgressed(Table table) {
		if (render) {
			System.out.print('.'); //NOSONAR
		}
	}

	@Override
	public long startedGraphingSummaries() {
        startedGraphingSummariesAt = System.currentTimeMillis();
        long duration = startedGraphingSummariesAt - startedConnectingAt;

        if (render) {
	        System.err.flush(); //NOSONAR
	        System.out.flush(); //NOSONAR
	        System.out.println("(" + duration / 1000 + "sec)"); //NOSONAR
	        System.out.print("Writing/graphing summary"); //NOSONAR
			System.out.print('.'); //NOSONAR
        }

        return duration;
	}

	@Override
	public void graphingSummaryProgressed() {
		if (render) {
			System.out.print('.'); //NOSONAR
		}
	}

	@Override
	public long startedGraphingDetails() {
		startedGraphingDetailsAt = System.currentTimeMillis();
		long duration = startedGraphingDetailsAt - startedGraphingSummariesAt;

		if (render) {
	        System.err.flush(); //NOSONAR
	        System.out.flush(); //NOSONAR
        	System.out.println("(" + duration / 1000 + "sec)"); //NOSONAR
        	System.out.print("Writing/diagramming details"); //NOSONAR
		}

		return duration;
	}

	@Override
	public void graphingDetailsProgressed(Table table) {
		if (render) {
			System.out.print('.'); //NOSONAR
		}
	}

	@Override
	public long finishedGatheringDetails() {
		finishedAt = System.currentTimeMillis();
		long duration = finishedAt - startedGraphingDetailsAt;

		if (render) {
	        System.err.flush(); //NOSONAR
	        System.out.flush(); //NOSONAR
            System.out.println("(" + duration / 1000 + "sec)"); //NOSONAR
		}

		return duration;
	}

	@Override
	public long finished(Collection<Table> tables, Config config) {
		finishedAt = System.currentTimeMillis();
		long duration = finishedAt - startedAt;

		if (render) {
	        System.err.flush(); //NOSONAR
	        System.out.flush(); //NOSONAR
            System.out.println("Wrote relationship details of " + tables.size() + " tables/views to directory '" + commandLineArguments.getOutputDirectory() + "' in " + duration / 1000 + " seconds."); //NOSONAR
            System.out.println("View the results by opening " + new File(commandLineArguments.getOutputDirectory(), "index.html")); //NOSONAR
		}

		return duration;
	}

	@Override
	public String recoverableExceptionEncountered(String msg, Exception exc, String sql) {
        System.out.println(); //NOSONAR
        System.out.flush(); //NOSONAR

        String text = msg + ": " + exc;
        if (sql != null) {
        	text += ": " + sql;
        }

        return text;
	}
}

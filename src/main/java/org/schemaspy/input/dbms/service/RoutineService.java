/*
 * Copyright (C) 2019 Nils Petzaell
 * Copyright (C) 2019 Kamyab Nazari
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

import org.schemaspy.Config;
import org.schemaspy.model.Database;
import org.schemaspy.model.Routine;
import org.schemaspy.model.RoutineParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoutineService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SqlService sqlService;

	public RoutineService(SqlService sqlService) {
		this.sqlService = sqlService;
	}

	public void gatherRoutines(Config config, Database database) throws SQLException {
		initRoutines(config, database);
		initRoutineParameters(config, database);
	}

	private void initListOfRoutines(Config config, Database db, int count, List<String> listResultSetData)
			throws SQLException {
		String sql = config.getDbProperties().getProperty("selectRoutinesSql");

		int rowCounter = 0;

		if (sql != null) {
			try (PreparedStatement stmtSecond = sqlService.prepareStatement(sql, db, null);
					ResultSet rsSecond = stmtSecond.executeQuery()) {

				String stringSeqno = "";
				String stringProcname = "";
				String stringData = "";

				int indexProcname = rsSecond.findColumn("procname");
				int indexData = rsSecond.findColumn("data");
				int indexSeqno = rsSecond.findColumn("seqno");

				// Second ResultSet to save all of the Query Rows inside a List
				while (rsSecond.next()) {
					rowCounter = rowCounter + 1;
					if (indexProcname > 0 && indexData > 0 && indexSeqno > 0) {
						stringSeqno = rsSecond.getString(indexSeqno);
						stringData = rsSecond.getString(indexData);
						stringProcname = rsSecond.getString(indexProcname);
						listResultSetData.add(stringProcname + "§" + stringSeqno + "§" + stringData);
					}
				}
			}
		}
	}

	private void initListMaxSeqno(List<String> listResultSetData, List<String> listResultSetMaxSeqno, int count) {

		int maxSeqno = 1;

		for (int i = 0; i < count; i++) {
			if (i + 1 < count) {
				String[] parts = listResultSetData.get(i).split("§");
				String partProcname = parts[0];

				String[] laterParts = listResultSetData.get(i + 1).split("§");
				String laterPartProcname = laterParts[0];

				if (partProcname.equals(laterPartProcname)) {
					maxSeqno = maxSeqno + 1;

					laterPartProcname = "";
				} else {
					listResultSetMaxSeqno.add(partProcname + "§" + maxSeqno);
					maxSeqno = 1;
				}
			}
		}
	}

	private int giveMaxSeqnoForRoutineName(List<String> listResultSetMaxSeqno, String routineName) {
		int maxSeqno = 0;

		// get the maxSeq for the specific routineName
		for (int i = 0; i < listResultSetMaxSeqno.size(); i++) {
			String[] currentParts = listResultSetMaxSeqno.get(i).split("§");
			String currentPartProcname = currentParts[0];
			String currentPartMaxSeqno = currentParts[1];
			if (currentPartProcname.equals(routineName)) {
				maxSeqno = Integer.parseInt(currentPartMaxSeqno);
				break;
			}
		}
		return maxSeqno;
	}

	private String gatherDataColumn(List<String> listResultSetData, int rowNumber, int maxS, String routineName) {
		String fullData = "";
		int maxSeqno = maxS;

		int endSeqno = rowNumber + maxSeqno;

		// Join the Data' from the same seqno
		for (int i = rowNumber - 1; i < endSeqno; i++) {
			if (i + 1 < endSeqno) {

				String[] currentParts = listResultSetData.get(i).split("§");
				String currentPartProcname = currentParts[0];
				String currentPartData = currentParts[2];

				String[] laterParts = listResultSetData.get(i + 1).split("§");
				String laterPartProcname = laterParts[0];
				String laterPartData = laterParts[2];

				if (currentPartProcname.equals(routineName) && currentPartProcname.equals(laterPartProcname)) {
					if (fullData.equals("")) {
						fullData = currentPartData + laterPartData;
					} else {
						fullData = fullData + laterPartData;
					}
				} else if (currentPartProcname.equals(routineName) && currentPartProcname != laterPartProcname) {
					if (fullData.equals("")) {
						fullData = currentPartData;
					}
				}
			}
		}
		return fullData;
	}

	/**
	 * Initializes stored procedures / functions.
	 *
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	private void initRoutines(Config config, Database db) throws SQLException {
		String sql = config.getDbProperties().getProperty("selectRoutinesSql");
		String sqlCount = config.getDbProperties().getProperty("selectCountRoutine");

		List<String> listResultSetData = new ArrayList<String>();
		List<String> listResultSetMaxSeqno = new ArrayList<String>();

		int count = 0;
		int rowCounter = 0;

		String fullData = "";
		int tmp = 0;

		// Counting how many results have been given from the queries
		if (sqlCount != null) {
			try (PreparedStatement stmt = sqlService.prepareStatement(sqlCount, db, null);
					ResultSet rsCount = stmt.executeQuery()) {

				if (rsCount.next() == true) {
					count = rsCount.getInt("total");
				}
			}
		}

		// Creating the list from the given db and config with the number of row counts
		initListOfRoutines(config, db, count, listResultSetData);

		// Creating a new List with No redondant rows and also with maxSeqno
		initListMaxSeqno(listResultSetData, listResultSetMaxSeqno, count);

		if (sql != null) {
			try (PreparedStatement stmtFirst = sqlService.prepareStatement(sql, db, null);
					ResultSet rsFirst = stmtFirst.executeQuery()) {

				rowCounter = 0;
				while (rsFirst.next()) {

					rowCounter = rowCounter + 1;

					String routineName = rsFirst.getString("procname");

					String routineType = rsFirst.getString("isproc");
					if (routineType.equals("f")) {
						routineType = "FUNCTION";
					} else if (routineType.equals("t")) {
						routineType = "PROCEDURE";
					}
					String returnType = rsFirst.getString("paramstyle");
					if (returnType.equals("i") || returnType.equals("I")) {
						returnType = "IBM® Informix®";
					}

					String definitionLanguage = rsFirst.getString("langname");

					String definition = rsFirst.getString("data");

					// Getting the combined Data from the query seq's of a specific routineName
					if (rowCounter > 0) {
						int maxSeqno = giveMaxSeqnoForRoutineName(listResultSetMaxSeqno, routineName);
						if (tmp == 0) {
							tmp = maxSeqno + tmp;
						}
						if (tmp == maxSeqno) {
							fullData = gatherDataColumn(listResultSetData, rowCounter, maxSeqno, routineName);
							definition = fullData;
							tmp = tmp - 1;
						} else if (tmp != 0 && tmp != maxSeqno) {
							definition = fullData;
							tmp = tmp - 1;
						} else {
							fullData = "";
						}
					}

					String dataAccess = null;

					String securityType = rsFirst.getString("mode");
					if (securityType.equals("d") || securityType.equals("D")) {
						securityType = "DBA";
					} else if (securityType.equals("o") || securityType.equals("O")) {
						securityType = "Owner";
					} else if (securityType.equals("p") || securityType.equals("P")) {
						securityType = "Protected";
					} else if (securityType.equals("r") || securityType.equals("R")) {
						securityType = "Restricted";
					} else if (securityType.equals("t") || securityType.equals("T")) {
						securityType = "Trigger";
					}

					boolean deterministic = rsFirst.getBoolean("variant");

					// String comment = getOptionalString(rs, "owner");
					String comment = "No Comments";

					Routine routine = new Routine(routineName, routineType, returnType, definitionLanguage, definition,
							deterministic, dataAccess, securityType, comment);
					db.getRoutinesMap().put(routineName, routine);
				}

			} catch (SQLException sqlException) {
				// don't die just because this failed
				LOGGER.warn("Failed to retrieve stored procedure/function details using sql '{}'", sql, sqlException);
			}
		}
	}

	private void initRoutineParameters(Config config, Database db) {
		String sql = config.getDbProperties().getProperty("selectRoutineParametersSql");

		if (sql != null) {

			try (PreparedStatement stmt = sqlService.prepareStatement(sql, db, null);
					ResultSet rs = stmt.executeQuery()) {

				while (rs.next()) {
					String routineName = rs.getString("procname");

					Routine routine = db.getRoutinesMap().get(routineName);
					if (routine != null) {
						String paramName = rs.getString("paramname");
						if (paramName == null) {
							paramName = "null";
						}
						String type = rs.getString("paramtype");
						switch (type) {

						case "0":
							type = "CHAR";
							break;
						case "1":
							type = "SMALLINT";
							break;
						case "2":
							type = "INTEGER";
							break;
						case "3":
							type = "FLOAT";
							break;
						case "4":
							type = "SMALLFLOAT";
							break;
						case "5":
							type = "DECIMAL";
							break;
						case "6":
							type = "SERIAL";
							break;
						case "7":
							type = "DATE";
							break;
						case "8":
							type = "MONEY";
							break;
						case "9":
							type = "NULL";
							break;
						case "10":
							type = "DATETIME";
							break;
						case "11":
							type = "BYTE";
							break;
						case "12":
							type = "TEXT";
							break;
						case "13":
							type = "VARCHAR";
							break;
						case "14":
							type = "INTERVAL";
							break;
						case "15":
							type = "NCHAR";
							break;
						case "16":
							type = "NVARCHAR";
							break;
						case "17":
							type = "INT8";
							break;
						case "18":
							type = "SERIAL8";
							break;
						case "19":
							type = "SET";
							break;
						case "20":
							type = "MULTISET";
							break;
						case "21":
							type = "LIST";
							break;
						case "22":
							type = "ROW (unnamed)";
							break;
						case "23":
							type = "COLLECTION";
							break;
						case "40":
							type = "LVARCHAR fixed-length opaque types";
							break;
						case "41":
							type = "BLOB, BOOLEAN, CLOB variable-length opaque types";
							break;
						case "43":
							type = "LVARCHAR (client-side only)";
							break;
						case "45":
							type = "BOOLEAN";
							break;
						case "52":
							type = "BIGINT";
							break;
						case "53":
							type = "BIGSERIAL";
							break;
						case "2061":
							type = "IDSSECURITYLABEL";
							break;
						case "4118":
							type = "ROW (named)";
							break;
						default:
							type = "NOT KNOWN";
						}

						String mode = rs.getString("paramattr");
						if (mode.equals("0")) {
							mode = "Of unknown type";
						} else if (mode.equals("1")) {
							mode = "INPUT mode";
						} else if (mode.equals("2")) {
							mode = "INOUT mode";
						} else if (mode.equals("3")) {
							mode = "Multiple return value";
						} else if (mode.equals("4")) {
							mode = "OUT mode";
						} else if (mode.equals("5")) {
							mode = "A return value";
						}

						RoutineParameter param = new RoutineParameter(paramName, type, mode);
						routine.addParameter(param);
					}
				}
			} catch (SQLException sqlException) {
				// don't die just because this failed
				LOGGER.warn("Failed to retrieve stored procedure/function details using SQL '{}'", sql, sqlException);
			}
		}
	}

	private static String getOptionalString(ResultSet rs, String columnName) {
		try {
			return rs.getString(columnName);
		} catch (SQLException sqlException) {
			LOGGER.debug("Failed to get value for column '{}'", sqlException);
			return null;
		}
	}

}

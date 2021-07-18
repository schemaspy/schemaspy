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
package org.schemaspy.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * @author Nils Petzaell
 */
public class ResettingOutputCapture implements TestRule {
	private Matcher<String> matcher;

	@Override
	public Statement apply(Statement base, Description description) {
		return new RecordOutputStatement(base);
	}

	private class RecordOutputStatement extends Statement {
		private final Statement next;

		public RecordOutputStatement(Statement base) {
			next = base;
		}

		@Override
		public void evaluate() throws Throwable {
			final StreamProcessor sp = new StreamProcessor();
			sp.start();
			next.evaluate();
			checkIfMatches(sp.terminate());
		}
	}

	private void checkIfMatches(String captured) {
		MatcherAssert.assertThat(captured, matcher);
	}

	private static class StreamProcessor extends Thread {
		private final PipedInputStream pis;
		private final PipedOutputStream pos;
		private final PrintStream downstream;
		private final StringBuilder content = new StringBuilder();

		private StreamProcessor() {
			downstream = System.out;
			pos = new PipedOutputStream();
			try {
				pis = new PipedInputStream(pos);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		@Override
		public void run() {
			System.setOut(new PrintStream(pos));
			try (BufferedReader br = new BufferedReader(new InputStreamReader(pis));) {
				// once output is resotred, we must terminate
				while (true) {
					String line = br.readLine();
					if (line == null) {
						return;
					}
					content.append(line).append("\n");
					downstream.println(line);
				}
			} catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		public String terminate() throws InterruptedException, IOException {
			System.setOut(downstream);
			pos.close();
			join();
			return content.toString();
		}
	}

	public void expect(Matcher<String> matcher) {
		this.matcher = matcher;
	}
}

/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package sabazios.util.wala.viz;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import sabazios.util.U;

import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.labeled.LabeledGraph;
import com.ibm.wala.util.warnings.WalaException;

/**
 * utilities for interfacing with DOT
 */
public class DotUtil {

	/**
	 * possible output formats for dot
	 * 
	 */
	public static enum DotOutputType {
		PS, SVG, PDF, EPS
	}

	private static DotOutputType outputType = DotOutputType.PDF;

	private static int fontSize = 10;
	private static String fontColor = "black";
	private static String fontName = "Arial";

	public static void setOutputType(DotOutputType outType) {
		outputType = outType;
	}

	public static DotOutputType getOutputType() {
		return outputType;
	}

	private static String outputTypeCmdLineParam() {
		switch (outputType) {
		case PS:
			return "-Tps";
		case EPS:
			return "-Teps";
		case SVG:
			return "-Tsvg";
		case PDF:
			return "-Tpdf";
		default:
			Assertions.UNREACHABLE();
			return null;
		}
	}

	/**
	 * Some versions of dot appear to croak on long labels. Reduce this if so.
	 */
	private final static int MAX_LABEL_LENGTH = 200;

	/**
   */
	public static <T> void dotify(Graph<T> m, NodeDecorator decorator, String dotFile, String outputFile, String dotExe)
			throws WalaException {
		dotify(m, decorator, null, dotFile, outputFile, dotExe);
	}

	public static <T> void dotify(Graph<T> g, NodeDecorator labels, String title, String dotFile, String outputFile,
			String dotExe) throws WalaException {
		if (g == null) {
			throw new IllegalArgumentException("g is null");
		}
		File f = DotUtil.writeDotFile(g, labels, title, dotFile);
		spawnDot(dotExe, outputFile, f);
	}

	public static void spawnDot(String dotExe, String outputFile, File dotFile) throws WalaException {
		if (dotFile == null) {
			throw new IllegalArgumentException("dotFile is null");
		}
		String[] cmdarray = { dotExe, outputTypeCmdLineParam(), "-o", outputFile, dotFile.getAbsolutePath() };
		System.out.println("spawning process " + Arrays.toString(cmdarray));
		BufferedInputStream output = null;
		BufferedInputStream error = null;
		try {
			Process p = Runtime.getRuntime().exec(cmdarray);
			output = new BufferedInputStream(p.getInputStream());
			error = new BufferedInputStream(p.getErrorStream());
			boolean repeat = true;
			while (repeat) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					// just ignore and continue
				}
				if (output.available() > 0) {
					byte[] data = new byte[output.available()];
					int nRead = output.read(data);
					System.err.println("read " + nRead + " bytes from output stream:\n"
							+ new String(data, Charset.forName("US-ASCII")));
				}
				if (error.available() > 0) {
					byte[] data = new byte[error.available()];
					int nRead = error.read(data);
					System.err.println("read " + nRead + " bytes from error stream:\n"
							+ new String(data, Charset.forName("US-ASCII")));
				}
				try {
					p.exitValue();
					// if we get here, the process has terminated
					repeat = false;
					System.out.println("process terminated with exit code " + p.exitValue());
				} catch (IllegalThreadStateException e) {
					// this means the process has not yet terminated.
					repeat = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new WalaException("IOException in " + DotUtil.class);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (error != null) {
				try {
					error.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static <T> File writeDotFile(Graph<T> g, NodeDecorator labels, String title, String dotfile)
			throws WalaException {

		if (g == null) {
			throw new IllegalArgumentException("g is null");
		}
		StringBuffer dotStringBuffer = dotOutput(g, labels, title);

		// retrieve the filename parameter to this component, a String
		if (dotfile == null) {
			throw new WalaException("internal error: null filename parameter");
		}
		try {
			File f = new File(dotfile);
			FileWriter fw = new FileWriter(f);
			fw.write(dotStringBuffer.toString());
			fw.close();
			return f;

		} catch (Exception e) {
			throw new WalaException("Error writing dot file " + dotfile);
		}
	}

	/**
	 * @return StringBuffer holding dot output representing G
	 * @throws WalaException
	 */
	private static <T> StringBuffer dotOutput(Graph<T> g, NodeDecorator labels, String title) throws WalaException {
		StringBuffer result = new StringBuffer("digraph \"DirectedGraph\" {\n");

		if (title != null) {
			result.append("graph [label = \"" + title + "\", labelloc=t, concentrate = true];");
		} else {
			result.append("graph [concentrate = true];");
		}

		String rankdir = getRankDir();
		if (rankdir != null) {
			result.append("rankdir=" + rankdir + ";");
		}
		String fontsizeStr = "fontsize=" + fontSize;
		String fontcolorStr = (fontColor != null) ? ",fontcolor=" + fontColor : "";
		String fontnameStr = (fontName != null) ? ",fontname=" + fontName : "";

		result.append("center=true;");
		result.append(fontsizeStr);
		result.append(";node [ ");
		result.append(fontsizeStr);
		result.append(fontcolorStr);
		result.append(fontnameStr);
		result.append("];edge [ color=black,");
		result.append(fontsizeStr);
		result.append(fontcolorStr);
		result.append(fontnameStr);
		result.append("]; \n");

		Collection dotNodes = computeDotNodes(g);

		outputNodes(labels, result, dotNodes);

		for (Iterator<? extends T> it = g.iterator(); it.hasNext();) {
			T n = it.next();
			for (Iterator<? extends T> it2 = g.getSuccNodes(n); it2.hasNext();) {
				T s = it2.next();
				if (g instanceof LabeledGraph<?, ?>) {
					@SuppressWarnings("unchecked")
					LabeledGraph<T, ?> lg = (LabeledGraph<T, ?>) g;
					Set<?> edgeLabels = lg.getEdgeLabels(n, s);
					for (Object l : edgeLabels) {
						result.append(" ");
						result.append(getPort(n, labels));
						result.append(" -> ");
						result.append(getPort(s, labels));
						result.append("[label=\"");
						result.append(U.tos(l));
						result.append("\"]");
					}
					result.append(" \n");
				} else {
					result.append(" ");
					result.append(getPort(n, labels));
					result.append(" -> ");
					result.append(getPort(s, labels));
					result.append(" \n");
				}

			}
		}

		result.append("\n}");
		return result;
	}

	private static void outputNodes(NodeDecorator labels, StringBuffer result, Collection dotNodes)
			throws WalaException {
		for (Iterator it = dotNodes.iterator(); it.hasNext();) {
			Object next = it.next();
			if(labels == null || labels.shouldDisplay(next))
				outputNode(labels, result, next);
		}
	}

	private static void outputNode(NodeDecorator labels, StringBuffer result, Object n) throws WalaException {
		result.append("   ");
		result.append("\"");
		result.append(getLabel(n, labels));
		result.append("\"");
		result.append(decorateNode(n, labels));
	}

	/**
	 * Compute the nodes to visualize
	 */
	private static <T> Collection<T> computeDotNodes(Graph<T> g) throws WalaException {
		return Iterator2Collection.toSet(g.iterator());
	}

	private static String getRankDir() throws WalaException {
		return null;
	}

	/**
	 * @param n
	 *            node to decorate
	 * @param d
	 *            decorating master
	 */
	private static String decorateNode(Object n, NodeDecorator d) throws WalaException {
		StringBuffer result = new StringBuffer();
		if(d != null)
			result.append(" ["+d.getDecoration(n)+" ]\n");
		else
			result.append(" [ ]\n");
		return result.toString();
	}

	private static String getLabel(Object o, NodeDecorator d) throws WalaException {
		String result = null;
		if (d == null) {
			result = o.toString();
		} else {
			result = d.getLabel(o);
			result = result == null ? o.toString() : result;
		}
		if (result.length() >= MAX_LABEL_LENGTH) {
			result = result.substring(0, MAX_LABEL_LENGTH - 3) + "...";
		}
		return result;
	}

	private static String getPort(Object o, NodeDecorator d) throws WalaException {
		return "\"" + getLabel(o, d) + "\"";

	}

	public static int getFontSize() {
		return fontSize;
	}

	public static void setFontSize(int fontSize) {
		DotUtil.fontSize = fontSize;
	}

}

package com.gikk.util;

/**
 *
 * @author Gikkman
 */
public class StackTrace
{

	/**
	 * Prints a message, and appends a stack trace link.
	 * <p>
	 * This will allow the user to click a link in the IDE to go directly to the
	 * stack trace position.
	 *
	 * @param message The message to print
	 */
	public static void error(String message)
	{
		System.err.println(message + " - " + getStackPos());
	}

	/**
	 * Returns a link to a code position.
	 * Intended to be inserted in an error message.
	 * <br>This will produce an error message in the output console with a clickable link that opens the file that caused the error.
	 * <p>
	 * <br><br><b>Example</b>
	 * <br><code>System.err.println("Error. Unknown args. " + StackTrace.getStackPos() );</code>
	 *
	 * @return A stack trace position link
	 */
	public static String getStackPos()
	{
		String out = "   ";
		StackTraceElement[] e = new Exception().getStackTrace();

		for (int i = 3; i < e.length && i < 6; i++)
		{
			String s = e[i].toString();
			int f = s.indexOf("(");
			int l = s.lastIndexOf(")") + 1;
			out += s.substring(f, l) + " ";
		}
		return out;
	}
}

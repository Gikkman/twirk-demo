package com.gikk.util;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 *
 * @author Gikkman
 */
public class Log {

    /**
     * *************************************************************************
     * STATIC INIT
	 *************************************************************************
     */
    private static final PrintStream err;

    static {
        err = getPrintStream();
    }

    private static PrintStream getPrintStream() {
        try {
            return new PrintStream(System.out, true, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            PrintStream str = System.err;
            str.println(buildMessage(Type.Warning, "Cannot bind Log to UTF-8", ex));
            return str;
        }
    }

    /**
     * *************************************************************************
     * PUBLIC
	 *************************************************************************
     */
    public static void trace(String message, Object... args) {
        err.println(buildMessage(Type.Trace, message, args));
    }

    public static void debug(String message, Object... args) {
        err.println(buildMessage(Type.Debug, message, args));
    }

    public static void info(String message, Object... args) {
        err.println(buildMessage(Type.Info, message, args));
    }

    public static void warning(String message, Object... args) {
        err.println(buildMessage(Type.Warning, message, args));
    }

    public static void error(String message, Object... args) {
        err.println(buildMessage(Type.Error, message, args));
    }

    private static String buildMessage(Type t, String message, Object... args) {
        StringBuilder builder = new StringBuilder();
        builder.append(t.code).append(" - ").append(message).append(" : ").append(StackTrace.getStackPos());
        for (Object o : args) {
            String obj = "";
            if (o == null) {
                obj = "null";
            } else if (o.getClass().isArray()) {
                obj = Arrays.deepToString((Object[]) o);
            } else {
                obj = o.toString();
            }
            builder.append("\n").append(t.code).append("\t").append(obj);
        }
        return builder.toString();
    }

    /**
     * *************************************************************************
     * INTERNAL
	 *************************************************************************
     */
    enum Type {
        Trace("T"), Debug("D"), Info("I"), Warning("W"), Error("E");
        private final String code;

        private Type(String s) {
            code = s;
        }
    }
}

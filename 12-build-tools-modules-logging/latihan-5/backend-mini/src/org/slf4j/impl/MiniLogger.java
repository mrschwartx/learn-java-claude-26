package org.slf4j.impl;

import java.io.PrintStream;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/** Backend SLF4J 1.7 buatan sendiri (untuk demo): format satu-baris JSON-ish, level per package, mendukung MDC. */
public class MiniLogger extends MarkerIgnoringBase {
    static final int TRACE = 0, DEBUG = 1, INFO = 2, WARN = 3, ERROR = 4;
    static final String[] NAMA = { "TRACE", "DEBUG", "INFO", "WARN", "ERROR" };
    private final int ambang;
    private final PrintStream out = System.out;

    MiniLogger(String name) {
        this.name = name;
        // aturan mirip logback.xml: package com.contoh = DEBUG, selain itu INFO
        this.ambang = name.startsWith("com.contoh") ? DEBUG : INFO;
    }

    private void tulis(int level, FormattingTuple t) {
        if (level < ambang) return;
        String req = MDC.get("requestId");
        out.println("{\"level\":\"" + NAMA[level] + "\",\"logger\":\"" + name.substring(name.lastIndexOf('.') + 1)
                + "\",\"requestId\":" + (req == null ? "null" : "\"" + req + "\"")
                + ",\"msg\":\"" + t.getMessage().replace("\"", "'") + "\"}");
        if (t.getThrowable() != null) out.println("  stack: " + t.getThrowable());
    }

    private void f(int level, String fmt, Object... a) { if (level >= ambang) tulis(level, MessageFormatter.arrayFormat(fmt, a)); }

    public boolean isTraceEnabled() { return ambang <= TRACE; }
    public boolean isDebugEnabled() { return ambang <= DEBUG; }
    public boolean isInfoEnabled()  { return ambang <= INFO; }
    public boolean isWarnEnabled()  { return ambang <= WARN; }
    public boolean isErrorEnabled() { return ambang <= ERROR; }

    public void trace(String m) { f(TRACE, m); }
    public void trace(String m, Object a) { f(TRACE, m, a); }
    public void trace(String m, Object a, Object b) { f(TRACE, m, a, b); }
    public void trace(String m, Object... a) { f(TRACE, m, a); }
    public void trace(String m, Throwable t) { f(TRACE, m, t); }
    public void debug(String m) { f(DEBUG, m); }
    public void debug(String m, Object a) { f(DEBUG, m, a); }
    public void debug(String m, Object a, Object b) { f(DEBUG, m, a, b); }
    public void debug(String m, Object... a) { f(DEBUG, m, a); }
    public void debug(String m, Throwable t) { f(DEBUG, m, t); }
    public void info(String m) { f(INFO, m); }
    public void info(String m, Object a) { f(INFO, m, a); }
    public void info(String m, Object a, Object b) { f(INFO, m, a, b); }
    public void info(String m, Object... a) { f(INFO, m, a); }
    public void info(String m, Throwable t) { f(INFO, m, t); }
    public void warn(String m) { f(WARN, m); }
    public void warn(String m, Object a) { f(WARN, m, a); }
    public void warn(String m, Object a, Object b) { f(WARN, m, a, b); }
    public void warn(String m, Object... a) { f(WARN, m, a); }
    public void warn(String m, Throwable t) { f(WARN, m, t); }
    public void error(String m) { f(ERROR, m); }
    public void error(String m, Object a) { f(ERROR, m, a); }
    public void error(String m, Object a, Object b) { f(ERROR, m, a, b); }
    public void error(String m, Object... a) { f(ERROR, m, a); }
    public void error(String m, Throwable t) { f(ERROR, m, t); }
}

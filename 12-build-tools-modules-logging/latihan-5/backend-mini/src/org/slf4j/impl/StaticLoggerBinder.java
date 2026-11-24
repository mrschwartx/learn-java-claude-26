package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

public class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
    public static String REQUESTED_API_VERSION = "1.7.36";
    public static StaticLoggerBinder getSingleton() { return SINGLETON; }
    private final ConcurrentHashMap<String, Logger> peta = new ConcurrentHashMap<>();
    private final ILoggerFactory pabrik = nama -> peta.computeIfAbsent(nama, MiniLogger::new);
    public ILoggerFactory getLoggerFactory() { return pabrik; }
    public String getLoggerFactoryClassStr() { return pabrik.getClass().getName(); }
}

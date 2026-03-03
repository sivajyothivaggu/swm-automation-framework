package com.swm.core.utils;

import org.apache.logging.log4j.*;

public class LoggerUtils {
    private static final Logger logger = LogManager.getLogger(LoggerUtils.class);
    
    public static void info(String message) {
        logger.info(message);
    }
    
    public static void error(String message) {
        logger.error(message);
    }
    
    public static void warn(String message) {
        logger.warn(message);
    }
    
    public static void debug(String message) {
        logger.debug(message);
    }
}

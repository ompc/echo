package com.github.ompc.echo.server.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.String.format;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * 日志工具类
 * Created by vlinux on 15/2/14.
 */
public class LogUtils {

    private static final Logger logger = createLogger();

    private static Logger createLogger() {
        final String logFilePath = "./echo.log";
        final Logger logger = Logger.getAnonymousLogger();
        try {

            final FileHandler fileHandler = new FileHandler(logFilePath);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.setUseParentHandlers(false);
            logger.addHandler(fileHandler);
            logger.log(INFO, format("init logger file success, file=%s",logFilePath));
        } catch (IOException e) {
            logger.log(WARNING,format("init logger file failed, file=%s",logFilePath));
        }
        return logger;
    }

    /**
     * info级日志
     *
     * @param format 日志格式,同String.format()
     * @param args   日志参数,同String.format()
     */
    public static void info(String format, Object... args) {
        if (logger.isLoggable(INFO)) {
            logger.log(INFO, format(format, args));
        }
    }

    /**
     * info级日志
     *
     * @param t      异常信息
     * @param format 日志格式,同String.format()
     * @param args   日志参数,同String.format()
     */
    public static void info(Throwable t, String format, Object... args) {
        if (logger.isLoggable(INFO)) {
            logger.log(INFO, format(format, args), t);
        }
    }


    /**
     * warn级日志
     *
     * @param format 日志格式,同String.format()
     * @param args   日志参数,同String.format()
     */
    public static void warn(String format, Object... args) {
        if (logger.isLoggable(WARNING)) {
            logger.log(WARNING, format(format, args));
        }
    }

    /**
     * warn级日志
     *
     * @param t      异常信息
     * @param format 日志格式,同String.format()
     * @param args   日志参数,同String.format()
     */
    public static void warn(Throwable t, String format, Object... args) {
        if (logger.isLoggable(WARNING)) {
            logger.log(WARNING, format(format, args), t);
        }
    }

    /**
     * error级日志
     *
     * @param t      异常信息
     * @param format 日志格式,同String.format()
     * @param args   日志参数,同String.format()
     */
    public static void error(Throwable t, String format, Object... args) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.log(Level.SEVERE, format(format, args), t);
        }
    }

}

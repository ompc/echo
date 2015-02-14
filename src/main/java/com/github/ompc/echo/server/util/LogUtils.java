package com.github.ompc.echo.server.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * 日志工具类
 * Created by vlinux on 15/2/14.
 */
public class LogUtils {

    private static final Logger logger = Logger.getAnonymousLogger();

    /**
     * info级日志
     *
     * @param format 日志格式,同String.format()
     * @param args   日志参数,同String.format()
     */
    public static void info(String format, Object... args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, format(format, args));
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
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, format(format, args), t);
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
        if (logger.isLoggable(Level.WARNING)) {
            logger.log(Level.WARNING, format(format, args), t);
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

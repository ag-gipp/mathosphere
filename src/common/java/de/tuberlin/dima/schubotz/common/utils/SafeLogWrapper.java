package de.tuberlin.dima.schubotz.common.utils;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import java.io.Serializable;
import java.io.StringBufferInputStream;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Wraps around an Apache Commons log interface to provide log level guards as well as level customization.
 * Takes in any objects, removes all throwables and sends the last one, and then takes the remaining
 * objects and runs {@link SafeLogWrapper#buildString(java.util.HashSet)}
 */
public class SafeLogWrapper implements Serializable {
    private final Log logger;
    private final Class logClass;

    public static SafeLogWrapperLevel level = SafeLogWrapperLevel.DEBUG;


    /**
     * @param logClass class to which this log belongs
     */
    public SafeLogWrapper(Class logClass) {
        logger = LogFactory.getLog(logClass);
        this.logClass = logClass;
    }
    /**
     * Set level for the logger. This is a hack due to commons not allowing you to set the level.
     * @param level specify this using SafeLogWrapper level fields (e.g. SafeLogWrapper.FATAL)
     */
    public void setLevel(SafeLogWrapperLevel level) {
        this.level = level;
    }
    public void fatal(Object... params) {
        if (logger.isFatalEnabled() && level.compareTo(SafeLogWrapperLevel.FATAL) <= 0) {
            outputMsg(params);
        }
    }
    public void error(Object... params) {
        if (logger.isFatalEnabled() && level.compareTo(SafeLogWrapperLevel.ERROR) <= 0) {
            outputMsg(params);
        }
    }
    public void warn(Object... params) {
        if (logger.isWarnEnabled() && level.compareTo(SafeLogWrapperLevel.WARN) <= 0) {
            outputMsg(params);
        }
    }
    public void info(Object... params) {
        if (logger.isInfoEnabled() && level.compareTo(SafeLogWrapperLevel.INFO) <= 0) {
            outputMsg(params);
        }
    }
    public void debug(Object... params) {
        if (logger.isDebugEnabled() && level.compareTo(SafeLogWrapperLevel.DEBUG) <= 0) {
            outputMsg(params);
        }
    }
    private void outputMsg(Object... params) {
        HashSet<Object> paramSet = new HashSet<>(Arrays.asList(params));
            //Remove any throwables from the set
            Throwable ex = removeThrowable(paramSet);
            String msg = buildString(paramSet);
            logger.fatal(msg, ex);
    }
    private static String buildString(HashSet<Object> params) {
        StringBuilder builder = new StringBuilder();
        for (final Object param : params) {
            //All objects implement toString
            builder.append(param.toString());
        }
        return builder.toString();

    }
    private static Throwable removeThrowable(HashSet<Object> params) {
        Throwable out = null;
        for (final Object param : params) {
            if (param instanceof Throwable) {
                out = (Throwable) param;
                params.remove(param);
            }
        }
        return out;
    }

    /**
     * To check if a level is below another level, use <code>level.compareTo(anotherLevel) < 0</code>
     */
    public enum SafeLogWrapperLevel {
        DEBUG, INFO, WARN, ERROR, FATAL;
    }
}

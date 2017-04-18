package com.yusheng123.util;

import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.yusheng123.BuildConfig;

/**
 * Created by Monty on 2017/4/18.
 */

public class LogTool {
    private static String className;//类名
    private static String methodName;//方法名
    private static int lineNumber;//行数

    /**
     * isWrite:用于开关是否吧日志写入txt文件中</p>
     */
    private static final boolean isWrite = false;
    /**
     * isDebug :是用来控制，是否打印日志
     */
    private static final boolean isDeBug = true;
    /**
     * 存放日志文件的所在路径
     */
    private static final String DIRPATH = "";
    // private static final String DIRPATH = "/log";
    /**
     * 存放日志的文本名
     */
    private static final String LOGNAME = "";
    // private static final String LOGNAME = "log.txt";
    /**
     * 设置时间的格式
     */
    private static final String INFORMAT = "yyyy-MM-dd HH:mm:ss";

    private LogTool() {
        /* Protect from instantiations */
    }

    public static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    private static String createLog(String log) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(methodName);
        buffer.append("(").append(className).append(":").append(lineNumber).append(")");
        buffer.append(log);
        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }


    public static void e(String message) {
        if (!isDebuggable())
            return;

        // Throwable instance must be created before any methods
        getMethodNames(new Throwable().getStackTrace());
        String logStr = createLog(message);
        Log.e(className, logStr);

        if (isWrite) {
            write(logStr);
        }
    }


    public static void i(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        String logStr = createLog(message);
        Log.i(className, logStr);

        if (isWrite) {
            write(logStr);
        }
    }

    public static void d(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        String logStr = createLog(message);
        Log.d(className, logStr);

        if (isWrite) {
            write(logStr);
        }
    }

    public static void v(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        String logStr = createLog(message);
        Log.v(className, logStr);

        if (isWrite) {
            write(logStr);
        }
    }

    public static void w(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        String logStr = createLog(message);
        Log.w(className, logStr);

        if (isWrite) {
            write(logStr);
        }
    }


    /**
     * 用于把日志内容写入制定的文件
     *
     * @param @param tag 标识
     * @param @param msg 要输出的内容
     * @return void 返回类型
     * @throws
     */
    public static void write(String msg) {
        String path = FileUtil.createMkdirsAndFiles(DIRPATH, LOGNAME);
        if (TextUtils.isEmpty(path)) {
            return;
        }
        String log = DateFormat.format(INFORMAT, System.currentTimeMillis())
                + msg
                + "\n=================================分割线=================================";
        FileUtil.write2File(path, log, true);
    }


}

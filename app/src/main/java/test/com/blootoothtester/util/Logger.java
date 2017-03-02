package test.com.blootoothtester.util;

import android.util.Log;

public class Logger {
    /**
     * debug level log
     */
    public void d(String tag, String message) {
        Log.d(tag, message);
    }
    /**
     * error level log
     */
    public void e(String tag, String message, Exception e) {
        Log.e(tag, message, e);
    }
    /**
     * error level log
     */
    public void e(String tag, String message) {
        Log.e(tag, message);
    }
}

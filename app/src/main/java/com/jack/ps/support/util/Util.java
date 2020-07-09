package com.jack.ps.support.util;

import android.os.Process;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Jacky on 2020/7/9
 */
public class Util {

    public static void i(String tag, String format, Object... obj) {
        final String name = String.format(Locale.ENGLISH, "[pId:%d][t:%s]",
            Process.myPid(), Thread.currentThread().getName());
        if (obj != null && obj.length > 0) {
            Log.i(tag, name + "," + String.format(format, obj));
            return;
        }

        Log.i(tag, name + "," + format);
    }
}

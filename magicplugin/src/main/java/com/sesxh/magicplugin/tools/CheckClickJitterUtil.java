package com.sesxh.magicplugin.tools;


import com.sesxh.magicplugin.annotation.IgnoreHookMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LYH
 * @date 2021/7/13
 * @time 15:38
 * @desc 点击防抖
 **/
@IgnoreHookMethod
public class CheckClickJitterUtil {
    public static long sDuration = 300;
    private static Map<String, Long> records = new HashMap<>();

    public static boolean isClickJitter() {
        if (records.size() > 300) {
            records.clear();
        }
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        String key = ste.getClassName() + ste.getLineNumber();

        Long lastClickTime = records.get(key);
        long thisClickTime = System.currentTimeMillis();
        records.put(key, thisClickTime);
        if (lastClickTime == null) {
            lastClickTime = 0L;
        }
        long timeDuration = thisClickTime - lastClickTime;
        return 0 < timeDuration && timeDuration < sDuration;
    }

}

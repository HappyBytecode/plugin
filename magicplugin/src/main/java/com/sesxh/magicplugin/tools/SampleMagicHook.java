package com.sesxh.magicplugin.tools;

import android.os.SystemClock;
import android.util.Log;

import com.sesxh.magicplugin.annotation.IgnoreHookMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author LYH
 * @date 2021/1/6
 * @time 8:51
 * @desc 默认实现类
 */


@IgnoreHookMethod
public class SampleMagicHook implements IMagicHookHandler {
    protected static ThreadLocal<HashMap<String, Object>> local = new ThreadLocal<>();
    protected static final String LINE = "══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════";
    @Override
    public void onMethodEnter(String className,
                              String methodName,
                              String argsType,
                              String returnType,
                              String traceIdIndex,
                              String   nodeIdIndex,
                              String valueIndex,
                              Object... args) {
        String name = className + methodName + returnType + argsType;
        HashMap<String, Object> map = local.get();
        if (map == null) {
            map = new HashMap<>(16);
            local.set(map);
        }
        SampleMagicHook.InnerClass data = (SampleMagicHook.InnerClass) map.get(name);
        if (data == null || data.time == null) {
            map.put(name, new SampleMagicHook.InnerClass(SystemClock.elapsedRealtime()));
            return;
        }

        data.integer.incrementAndGet();
    }

    @Override
    public void onMethodReturn(Object returnObj,
                               String className,
                               String methodName,
                               String argsType,
                               String returnType,
                               String traceIdIndex,
                               String nodeIdIndex,
                               String valueIndex,
                               Object... args) {
        String name = className + methodName + returnType + argsType;
        Map map = local.get();
        SampleMagicHook.InnerClass data = null;
        if (map != null) {
            data = (SampleMagicHook.InnerClass) map.get(name);
        }
        if (data == null || data.time == null) {
            Log.d("MagicHookHandler", name + " <-- " + "not has data !");
            return;
        }

        if (data.integer.decrementAndGet() <= 0) {
            map.remove(name);
            if (map.size() == 0) {
                local.remove();
            }
            long time = SystemClock.elapsedRealtime() - data.time;
            int traceIdIndexI=Integer.valueOf(traceIdIndex);
            int nodeIdIndexI=Integer.valueOf(traceIdIndex);
            int valueIndexI=Integer.valueOf(traceIdIndex);
            int maxIndexI=Math.max(Math.max(traceIdIndexI,nodeIdIndexI),valueIndexI);
            Object traceId=null;
            Object nodeId=null;
            Object value=null;
            if(maxIndexI< args.length){
                if(traceIdIndexI!=-1){
                    traceId=args[traceIdIndexI];
                }
                if(nodeIdIndexI!=-1){
                    nodeId=args[nodeIdIndexI];
                }
                if(valueIndexI!=-1){
                    value=args[valueIndexI];
                }
            }
            StringBuilder msgBuilder = new StringBuilder(16 * 10)
                    .append(" ")
                    .append("\n╔").append(LINE)
                    .append("\n║ [Thread]:").append(Thread.currentThread().getName())
                    .append("\n║ [Class]:").append(className)
                    .append("\n║ [Method]:").append(methodName)
                    .append("\n║ [ArgsType]:").append(argsType)
                    .append("\n║ [ArgsValue]:").append(getArgsValue(args))
                    .append("\n║ [Return]:").append(returnObj==null?returnObj:returnObj.toString())
                    .append("\n║ [ReturnType]:").append(returnType)
                    .append("\n║ [TraceId]:").append(traceId)
                    .append("\n║ [NodeId]:").append(nodeId)
                    .append("\n║ [Value]:").append(value)
//                    .append("\n║ [TraceId]:").append(traceIdIndex==-1?null:args[traceIdIndex])
//                    .append("\n║ [NodeId]:").append(nodeIdIndex==-1?null:args[nodeIdIndex])
//                    .append("\n║ [Value]:").append(valueIndex==-1?null:args[valueIndex])
                    .append("\n║ [Time]:").append(time).append(" ms")
                    .append("\n╚").append(LINE);
            String msg = msgBuilder.toString();


            int i = 100;
            int w = 300;
            int e = 500;
            if (time > (long) i) {
                if (time <= (long)w) {
                    Log.i("MagicHookHandler", msg);
                } else if (time <= (long)e) {
                    Log.w("MagicHookHandler", msg);
                } else {
                    Log.e("MagicHookHandler", msg);
                }
            }
        }
    }

    protected String getArgsValue(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            for (int j = 0; j < args.length; j++) {
                builder.append(args[j]==null?null:args[j].toString())
                        .append(j == args.length - 1 ? ']' : ',');
            }
            return builder.toString();
        }
    }

    protected String formatName(String name) {
        if (name.length() <= LINE.length() - 9) {
            return name;
        } else {
            return name.substring(0, LINE.length() - 9) + "\n|         " + formatName(name.substring(LINE.length() - 9));
        }
    }

    @IgnoreHookMethod
    protected static class InnerClass {
       public AtomicInteger integer;
       public Long time;

        InnerClass(Long time) {
            this.integer = new AtomicInteger(1);
            this.time = time;
        }
    }
}

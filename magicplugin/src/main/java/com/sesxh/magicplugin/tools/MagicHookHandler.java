package com.sesxh.magicplugin.tools;

import com.sesxh.magicplugin.annotation.IgnoreHookMethod;

/**
 * @author LYH
 **/
@IgnoreHookMethod
public class MagicHookHandler {
    /**
     * 如果设置了impl，要替换这个
     */
    private  static final IMagicHookHandler HANDLER=new SampleMagicHook();


    public static void enter(String className,
                             String methodName,
                             String argsType,
                             String returnType,
                             String traceIdIndex,
                             String nodeIdIndex,
                             String valueIndex,
                             Object... args
    ) {
        HANDLER.onMethodEnter(className, methodName, argsType, returnType,traceIdIndex,nodeIdIndex,valueIndex, args);
    }


    public static void exit(Object returnObj,
                            String className,
                            String methodName,
                            String argsType,
                            String returnType,
                            String traceIdIndex,
                            String nodeIdIndex,
                            String valueIndex,
                            Object... args) {
        HANDLER.onMethodReturn(returnObj,className, methodName, argsType, returnType, traceIdIndex,nodeIdIndex,valueIndex,args);
    }

}
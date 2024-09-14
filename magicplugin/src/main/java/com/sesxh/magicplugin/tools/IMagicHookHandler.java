package com.sesxh.magicplugin.tools;

/**
 * @author LYH
 **/
public interface IMagicHookHandler {

    void onMethodEnter(String className,
                       String methodName,
                       String argsType,
                       String returnType,
                       String traceIdIndex,
                       String nodeIdIndex,
                       String valueIndex,
                       Object... args
    );


    void onMethodReturn(Object returnObj,
                        String className,
                        String methodName,
                        String argsType,
                        String returnType,
                        String traceIdIndex,
                        String nodeIdIndex,
                        String valueIndex,
                        Object... args);
}

package com.sesxh.magicplugin.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author LYH
 * @date 2021/2/1
 * @time 14:14
 * @desc traceId
 **/

@Target(PARAMETER)
@Retention(RUNTIME)
public @interface NodeId {
}

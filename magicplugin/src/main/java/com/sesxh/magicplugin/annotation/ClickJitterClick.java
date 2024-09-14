package com.sesxh.magicplugin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author LYH
 **/
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface ClickJitterClick {
}

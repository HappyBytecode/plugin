package com.sesxh.plugin

import org.json.simple.JSONObject

/**
 * @author LYH
 **/

class MagicMethodHookConfig {
    //是否启用
    boolean enable = true
    //是否将所有的方法都统计，否则只统计注解和正则设置的
    boolean all = true
    //是否打印日志
    boolean log = true
    //jar包 名称 正则表达式,不设置默认不插桩jar包
    List<String> jarRegexs = []
    //类名称 正则表达式,class全名
    List<String> classRegexs = []
    //方法名称 正则表达式
    List<String> methodRegexs = []
    //是否用插桩后的jar包替换项目中的jar包
    boolean replaceJar = false
    //是否启用点击防抖
    boolean isClickJitter = true
    //是否启用耗时统计
    boolean isTimeStatistics = true
    String impl = ""
    String clickImpl=""


    @Override
    public String toString() {
        return toJson().toString()
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("enable", enable);
            jsonObject.put("all", all);
            jsonObject.put("log", log);
            jsonObject.put("jarRegexs", jarRegexs);
            jsonObject.put("classRegexs", classRegexs);
            jsonObject.put("methodRegexs", methodRegexs);
            jsonObject.put("replaceJar", replaceJar);
            jsonObject.put("isClickJitter", isClickJitter);
            jsonObject.put("isTimeStatistics", isTimeStatistics);
            jsonObject.put("impl", impl);
            jsonObject.put("clickImpl", clickImpl);
        } catch (Throwable e) {
            //JSONException
        }
        return jsonObject;
    }

}

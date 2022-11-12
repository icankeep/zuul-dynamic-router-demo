package com.passer.demo.zuuldynamicproxy.utils;

import com.google.gson.Gson;

/**
 * @author passer
 * @time 2022/11/12 22:41
 */
public class GsonUtils {
    private static final Gson gson = new Gson();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }
}
